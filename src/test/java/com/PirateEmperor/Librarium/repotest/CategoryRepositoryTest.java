package com.PirateEmperor.Librarium.repotest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.PirateEmperor.Librarium.model.Category;
import com.PirateEmperor.Librarium.model.CategoryRepository;

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class CategoryRepositoryTest {
	@Autowired
	private CategoryRepository crepository;

	@BeforeAll
	public void resetCategoryRepo() {
		crepository.deleteAll();
	}

	// CRUD tests
	@Test
	@Rollback
	public void testCreateCategory() {
		Category newCategory1 = this.createCategory("Other");
		assertThat(newCategory1.getCategoryid()).isNotNull();

		this.createCategory("IT");
		List<Category> categories = (List<Category>) crepository.findAll();
		assertThat(categories).hasSize(2);
	}

	private Category createCategory(String name) {
		Category newCategory = new Category(name);
		crepository.save(newCategory);

		return newCategory;
	}

	// Read functionalities tests
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Category> categories = (List<Category>) crepository.findAll();
		assertThat(categories).isEmpty();

		Optional<Category> optionalCategory = crepository.findById(Long.valueOf(2));
		assertThat(optionalCategory).isNotPresent();

		Category newCategory1 = this.createCategory("Other");
		Long newCategory1Id = newCategory1.getCategoryid();
		this.createCategory("IT");

		categories = (List<Category>) crepository.findAll();
		assertThat(categories).hasSize(2);

		optionalCategory = crepository.findById(newCategory1Id);
		assertThat(optionalCategory).isPresent();
	}

	@Test
	@Rollback
	public void testFindByName() {
		String nameToFindCategory = "Other";
		Optional<Category> optionalOther = crepository.findByName(nameToFindCategory);
		assertThat(optionalOther).isNotPresent();

		this.createCategory(nameToFindCategory);
		optionalOther = crepository.findByName(nameToFindCategory);
		assertThat(optionalOther).isPresent();
	}

	// Testing update functionalities:
	@Test
	@Rollback
	public void testUpdateCategory() {
		Category category = this.createCategory("Other");
		category.setName("IT");
		crepository.save(category);

		Optional<Category> optionalUpdatedCategory = crepository.findByName("IT");
		assertThat(optionalUpdatedCategory).isPresent();

		category = optionalUpdatedCategory.get();
		assertThat(category.getName()).isEqualTo("IT");
	}

	// Testing delete functionalities:
	@Test
	@Rollback
	public void testDeleteCategory() {
		Category categoryOther = this.createCategory("Other");
		Long categoryOtherId = categoryOther.getCategoryid();

		crepository.deleteById(categoryOtherId);
		List<Category> categories = (List<Category>) crepository.findAll();
		assertThat(categories).hasSize(0);

		this.createCategory("Other");
		this.createCategory("IT");

		crepository.deleteAll();
		categories = (List<Category>) crepository.findAll();
		assertThat(categories).hasSize(0);
	}
}
