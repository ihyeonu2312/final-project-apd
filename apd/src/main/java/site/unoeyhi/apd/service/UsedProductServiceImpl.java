package site.unoeyhi.apd.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import site.unoeyhi.apd.dto.usedproduct.UsedProductResponseDto;
import site.unoeyhi.apd.entity.UsedProduct;
import site.unoeyhi.apd.repository.UsedProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsedProductServiceImpl implements UsedProductService {

    private final UsedProductRepository usedProductRepository;

    @Override
    public UsedProduct createProduct(UsedProduct product) {
        return usedProductRepository.save(product);
    }

    @Override
    public Optional<UsedProduct> findById(Integer id) {
        return usedProductRepository.findById(id);
    }

    @Override
    public List<UsedProduct> findAll() {
        return usedProductRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        usedProductRepository.deleteById(id);
    }

    @Override
public List<UsedProductResponseDto> findAllDtos() {
    return usedProductRepository.findAll().stream()
        .map(product -> new UsedProductResponseDto(
            product.getUsedProductId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCondition().name(),
            product.getStatus().name(),
            product.getSeller().getNickname(), // seller → nickname
            product.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList()
        ))
        .toList();
}

}
