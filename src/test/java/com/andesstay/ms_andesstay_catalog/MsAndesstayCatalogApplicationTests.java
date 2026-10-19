package com.andesstay.ms_andesstay_catalog;

import com.andesstay.ms_andesstay_catalog.config.CatalogSeeder;
import com.andesstay.ms_andesstay_catalog.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MsAndesstayCatalogApplicationTests {

	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private CatalogSeeder seeder;

	@Test
	void contextLoadsYCargaLasUnidadesDeEjemploUnaSolaVez() {
		assertThat(unitRepository.count()).isEqualTo(6);

		seeder.run();

		assertThat(unitRepository.count()).isEqualTo(6);
	}

}
