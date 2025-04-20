package org.example.service;

import net.sourceforge.tess4j.Tesseract;
import org.example.model.Bill;
import org.example.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.example.model.Item;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    private Tesseract tesseract;

    public BillService() {
        System.setProperty("jna.library.path", "/opt/homebrew/lib");
        this.tesseract = new Tesseract();
    }

    public Bill createBillFromReceipt(MultipartFile file) throws Exception {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Check if file is an image
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (img == null) {
                throw new IllegalArgumentException("File is not a valid image");
            }

            tesseract.setDatapath( "/opt/homebrew/share/tessdata/");
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6); // Assume a single uniform block of text
            tesseract.setOcrEngineMode(1);
            tesseract.setTessVariable("user_defined_dpi", "300");
            tesseract.setTessVariable("debug_file", "/dev/null");

            String ocrResult = tesseract.doOCR(img);
            System.out.println("OCR raw result: " + ocrResult);
            
            // Parse the OCR result to extract bill information
            Bill bill = parseBillFromOcrText(ocrResult);
            return createBill(bill);

        } catch (IOException e) {
            throw new Exception("Failed to process image: " + e.getMessage());
        }
    }

    //Todo: Fix this logic
    private Bill parseBillFromOcrText(String ocrText) {
        Bill extractedBill = new Bill();
        extractedBill.setId(UUID.randomUUID().toString());
        
        // Extract restaurant name (usually at the top of the receipt)
        Pattern restaurantPattern = Pattern.compile("([\\w\\s&]+(?:Restaurant|Cafe|Bar|Grill|Bistro|Diner|Eatery)[\\w\\s&]*)", Pattern.CASE_INSENSITIVE);
        Matcher restaurantMatcher = restaurantPattern.matcher(ocrText);
        if (restaurantMatcher.find()) {
            extractedBill.setTitle(restaurantMatcher.group(1).trim());
        } 
        
        // Extract items and their prices (robust line-by-line extraction)
        List<Item> items = new ArrayList<>();
        String[] lines = ocrText.split("\\r?\\n");
        Pattern pricePattern = Pattern.compile("([0-9]+\\.[0-9]{2})");
        String lastItemName = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            String lower = line.toLowerCase();
            if (lower.isEmpty() || lower.contains("total") || lower.contains("subtotal") || lower.contains("tax") || lower.contains("service") || lower.contains("tip") || lower.contains("gratuity") || lower.contains("gst") || lower.contains("net amount")) {
                continue;
            }
            Matcher priceMatcher = pricePattern.matcher(line);
            // If line contains at least two prices, treat as price line
            List<String> prices = new ArrayList<>();
            while (priceMatcher.find()) {
                prices.add(priceMatcher.group(1));
            }
            if (prices.size() >= 1 && lastItemName != null) {
                // Use the last price as the amount
                try {
                    double price = Double.parseDouble(prices.get(prices.size() - 1));
                    Item item = new Item();
                    item.setId(UUID.randomUUID().toString());
                    item.setName(lastItemName);
                    item.setAmount(price);
                    items.add(item);
                } catch (NumberFormatException e) {
                    System.out.println("Could not parse price: " + prices.get(prices.size() - 1));
                }
                lastItemName = null;
            } else if (!line.matches(".*[0-9]+\\.[0-9]{2}.*")) {
                // If line does not contain a price, treat as item name
                lastItemName = line;
            }
        }
        
        extractedBill.setItems(items);
        
        // Set tax and service charge only if parsed from OCR text, otherwise leave as 0
        double taxAmount = 0.0;
        double serviceCharge = 0.0;
        
        // Try to extract tax from OCR text
        Pattern taxPattern = Pattern.compile("(?:tax|gst|vat)\\s*[:\\-]?\\s*([\\$£€]?\\s?[0-9]+\\.[0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher taxMatcher = taxPattern.matcher(ocrText);
        if (taxMatcher.find()) {
            try {
                String taxStr = taxMatcher.group(1).replaceAll("[^0-9.]", "");
                taxAmount = Double.parseDouble(taxStr);
            } catch (NumberFormatException e) {
                System.out.println("Could not parse tax amount, leaving as 0");
            }
        }
        extractedBill.setTax(taxAmount);
        
        // Try to extract service charge from OCR text
        Pattern servicePattern = Pattern.compile("(?:service|gratuity|tip)\\s*[:\\-]?\\s*([\\$£€]?\\s?[0-9]+\\.[0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher serviceMatcher = servicePattern.matcher(ocrText);
        if (serviceMatcher.find()) {
            try {
                String serviceStr = serviceMatcher.group(1).replaceAll("[^0-9.]", "");
                serviceCharge = Double.parseDouble(serviceStr);
            } catch (NumberFormatException e) {
                System.out.println("Could not parse service charge, leaving as 0");
            }
        }
        extractedBill.setServiceCharge(serviceCharge);
        
        // Calculate total amount from items, tax, and service charge
        double itemsTotal = items.stream().mapToDouble(Item::getAmount).sum();
        double totalAmount = itemsTotal + taxAmount + serviceCharge;
        
        // Try to extract total from OCR text
        Pattern totalPattern = Pattern.compile("(?:total|amount|sum)\\s*[:\\-]?\\s*([\\$£€]?\\s?[0-9]+\\.[0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher totalMatcher = totalPattern.matcher(ocrText);
        if (totalMatcher.find()) {
            try {
                String totalStr = totalMatcher.group(1).replaceAll("[^0-9.]", "");
                totalAmount = Double.parseDouble(totalStr);
            } catch (NumberFormatException e) {
                // Use calculated total if parsing fails
                System.out.println("Could not parse total amount, using calculated total");
            }
        }
        extractedBill.setTotalAmount(totalAmount);
        
        // Set creator name
        extractedBill.setCreatorName("Mr. Kim");
        
        // Set bill status and creation time
        extractedBill.setStatus("UNPAID");
        extractedBill.setCreatedAt(LocalDateTime.now());
        
        return extractedBill;
    }

    public Bill createBill(Bill bill) {
        return billRepository.save(bill);
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public Optional<Bill> getBillById(String id) {
        return billRepository.findById(id);
    }

    public Bill updateBill(String id, Bill billDetails) {
        return billRepository.findById(id)
                .map(bill -> {
                    bill.setTitle(billDetails.getTitle());
                    bill.setCreatorName(billDetails.getCreatorName());
                    bill.setItems(billDetails.getItems());
                    bill.setParticipants(billDetails.getParticipants());
                    bill.setTax(billDetails.getTax());
                    bill.setServiceCharge(billDetails.getServiceCharge());
                    bill.setTotalAmount(billDetails.getTotalAmount());
                    bill.setStatus(billDetails.getStatus());
                    bill.setCreatedAt(billDetails.getCreatedAt());
                    return billRepository.save(bill);
                })
                .orElseThrow(() -> new RuntimeException("Bill not found with id " + id));
    }

    public void deleteBill(String id) {
        billRepository.deleteById(id);
    }
}
