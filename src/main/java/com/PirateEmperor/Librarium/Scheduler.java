package com.PirateEmperor.Librarium;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.PirateEmperor.Librarium.model.Cart;
import com.PirateEmperor.Librarium.model.CartRepository;

@Component
public class Scheduler {
	@Autowired
	private CartRepository barepository;

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
	@Transactional
	public void deleteUnusedCarts() {
		List<Cart> carts = (List<Cart>) barepository.findAll();

		if (carts.size() > 0) {
			for (int i = 0; i < carts.size(); i++) {
				if (carts.get(i).getUser() == null && carts.get(i).isCurrent()) {
					if (LocalDate.now().isAfter(LocalDate.parse(carts.get(i).getExpiryDate()))) {
						barepository.delete(carts.get(i));
					}
				}
			}
		}
	}
}
