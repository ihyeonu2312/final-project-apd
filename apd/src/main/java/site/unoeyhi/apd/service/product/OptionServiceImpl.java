package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import site.unoeyhi.apd.entity.Option;
import site.unoeyhi.apd.repository.product.OptionRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionRepository;

    public OptionServiceImpl(OptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

    @Override
    public Option saveOption(Option option) {
        return optionRepository.save(option);
    }

    @Override
    public List<Option> getAllOptions() {
        return optionRepository.findAll();
    }

    @Override
    public Option getOptionById(Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 Option이 존재하지 않습니다: " + id));
    }

    @Override
    public void deleteOption(Long id) {
        optionRepository.deleteById(id);
    }
    @Override
    public Optional<Option> findByTypeAndValue(String optionValueType, String optionValue) {
        return optionRepository.findByOptionValueTypeAndOptionValue(optionValueType, optionValue);
    }
}
