package com.hrms.graphql.resolver;

import com.hrms.entity.City;
import com.hrms.graphql.input.CityInput;
import com.hrms.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CityResolver {

    private final CityRepository cityRepository;

    @QueryMapping
    public List<City> cities() {
        return cityRepository.findAll();
    }

    @QueryMapping
    public City city(@Argument Long id) {
        return cityRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<City> citiesByTenant(@Argument String tenantId) {
        return cityRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<City> citiesByState(@Argument String state) {
        return cityRepository.findByState(state);
    }

    @QueryMapping
    public List<City> activeCities() {
        return cityRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public City createCity(@Argument CityInput input) {
        City city = mapToEntity(input);
        return cityRepository.save(city);
    }

    @MutationMapping
    public City updateCity(@Argument Long id, @Argument CityInput input) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found"));

        updateEntityFromInput(city, input);
        return cityRepository.save(city);
    }

    @MutationMapping
    public Boolean deleteCity(@Argument Long id) {
        if (cityRepository.existsById(id)) {
            cityRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private City mapToEntity(CityInput input) {
        City city = new City();
        updateEntityFromInput(city, input);
        return city;
    }

    private void updateEntityFromInput(City city, CityInput input) {
        city.setTenantId(input.getTenantId());
        city.setCityName(input.getCityName());
        city.setState(input.getState());
        city.setPincode(input.getPincode());
        city.setIsActive(input.getIsActive());
    }
}
