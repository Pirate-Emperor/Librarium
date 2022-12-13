package com.PirateEmperor.Librarium.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.PirateEmperor.Librarium.model.Category;
import com.PirateEmperor.Librarium.model.CategoryRepository;

@Service
public class CategoryService {
	@Autowired
	private CategoryRepository categoryRepository;

	public List<Category> getCategories() {
		List<Category> categories = (List<Category>) categoryRepository.findAll();

		return categories;
	}
}
