package org.example.magazyn.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.magazyn.dto.ProductDto;
import org.example.magazyn.entity.Product;
import org.example.magazyn.entity.User;
import org.example.magazyn.repository.ProductRepository;
import org.example.magazyn.service.HistoryService;
import org.example.magazyn.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final HistoryService historyService;
    private final String UPLOAD_DIRECTORY = "magazyn/uploads/products";

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findByZoneIsNull() {
        return productRepository.findByZoneIsNull();
    }

    @Override
    public List<Product> getProductsByUser(User user) {
        return productRepository.findByUser(user);
    }

    @Override
    public Product addProduct(ProductDto productDto, User currentUser) throws IOException {
        Product product = mapDtoToProduct(productDto);
        product.setUser(currentUser);

        if (productDto.getImage() != null && !productDto.getImage().isEmpty()) {
            handleProductImage(product, productDto.getImage());
        }

        Product savedProduct = productRepository.save(product);

        historyService.addHistoryEntry(
                currentUser.getId(),
                "ADD_PRODUCT",
                "Dodano produkt: " + product.getName() + ", ID: " + savedProduct.getId()
        );

        return savedProduct;
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produkt nie znaleziony"));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        String productName = product.getName();
        Long productId = product.getId();
        User user = product.getUser();

        product.setQuantity(0);
        productRepository.save(product);

        historyService.addHistoryEntry(
                user.getId(),
                "DELETE_PRODUCT",
                "Usunięto produkt: " + productName + ", ID: " + productId
        );
    }

    @Override
    public Product updateProduct(Long id, ProductDto productDto) throws IOException {
        Product existingProduct = getProductById(id);
        User user = existingProduct.getUser();
        String oldName = existingProduct.getName();

        updateProductFromDto(existingProduct, productDto);

        if (productDto.getImage() != null && !productDto.getImage().isEmpty()) {
            deleteProductImage(existingProduct);
            handleProductImage(existingProduct, productDto.getImage());
        }

        Product updatedProduct = productRepository.save(existingProduct);

        historyService.addHistoryEntry(
                user.getId(),
                "UPDATE_PRODUCT",
                "Zaktualizowano produkt z \"" + oldName + "\" na \"" + updatedProduct.getName() + "\", ID: " + updatedProduct.getId()
        );

        return updatedProduct;
    }

    private Product mapDtoToProduct(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setCategory(productDto.getCategory());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setPurchasePrice(productDto.getPurchasePrice());
        product.setWeight(productDto.getWeight());
        product.setBrand(productDto.getBrand());
        product.setQuantity(productDto.getQuantity());
        return product;
    }

    private void updateProductFromDto(Product product, ProductDto productDto) {
        product.setName(productDto.getName());
        product.setCategory(productDto.getCategory());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setPurchasePrice(productDto.getPurchasePrice());
        product.setWeight(productDto.getWeight());
        product.setBrand(productDto.getBrand());
        product.setQuantity(productDto.getQuantity());
    }

    private void handleProductImage(Product product, MultipartFile image) throws IOException {
        String fileName = saveImageToFileSystem(image);
        product.setImageName(fileName);
        product.setImagePath("magazyn/uploads/products/" + fileName);
    }

    private String saveImageToFileSystem(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            originalFileName = "unnamed_file";
        }
        String cleanFileName = originalFileName
                .replaceAll("\\s+", "_")
                .replaceAll("[ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");

        String fileName = timestamp + "_" + cleanFileName;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }

    private void deleteProductImage(Product product) {
        if (product.getImagePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(product.getImagePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}