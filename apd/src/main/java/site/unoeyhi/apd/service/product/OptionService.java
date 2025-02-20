package site.unoeyhi.apd.service.product;

import java.util.List;
import java.util.Optional;

import site.unoeyhi.apd.entity.Option;

public interface OptionService {
    Option saveOption(Option option);
    List<Option> getAllOptions();
    Option getOptionById(Long id);
    void deleteOption(Long id);
    Optional<Option> findByTypeAndValue(String optionValueType, String optionValue);
    
}

