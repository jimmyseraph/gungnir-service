package vip.testops.gungnir.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import vip.testops.gungnir.entities.dto.Person;

public interface PersonRepository extends PagingAndSortingRepository<Person, String> {
}
