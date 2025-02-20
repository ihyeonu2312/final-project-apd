package site.unoeyhi.apd.repository.product;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.Option;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

  Optional<Option> findByOptionValueTypeAndOptionValue(String optionValueType, String optionValue);
}
