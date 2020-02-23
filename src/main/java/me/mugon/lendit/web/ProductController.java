package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.ProductService;
import me.mugon.lendit.domain.common.BaseValidator;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.CurrentUser;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.web.dto.product.ProductRequestDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping(value = "/api/products", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class ProductController {

    private final ProductService productService;

    private final BaseValidator baseValidator;

    @GetMapping
    public ResponseEntity<?> getProductList(Pageable pageable, PagedResourcesAssembler<Product> assembler) {
        return productService.getProductList(pageable, assembler);
    }

    @PostMapping
    public ResponseEntity<?> registrationProduct(@Valid @RequestBody ProductRequestDto productRequestDto, Errors errors, @CurrentUser Account currentUser) {
        if (errors.hasErrors()) {
            return baseValidator.returnErrors(errors);
        }
        return productService.registrationProduct(productRequestDto, currentUser);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductRequestDto productRequestDto, Errors errors, @CurrentUser Account currentUser) {
        if (errors.hasErrors()) {
            return baseValidator.returnErrors(errors);
        }
        return productService.updateProduct(productId, productRequestDto, currentUser);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, @CurrentUser Account currentUser) {
        return productService.deleteProduct(productId, currentUser);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }
}
