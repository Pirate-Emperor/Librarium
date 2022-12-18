package com.PirateEmperor.Librarium;

import com.PirateEmperor.Librarium.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.PirateEmperor.Librarium.model.Cart;

@SpringBootApplication
@EnableScheduling
public class MybooklistApplication {

	@Autowired
	private UserRepository urepository;

	@Autowired
	private CategoryRepository crepository;

	@Autowired
	private BookRepository repository;

	@Autowired
	private CartRepository barepository;

	@Autowired
	private CartBookRepository bbrepository;

	@Autowired
	private OrderRepository orepository;

	public static void main(String[] args) {
		SpringApplication.run(MybooklistApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner() {
		return (args) -> {
			Category thriller = new Category("Thriller");
			Category sciFi = new Category("Science fiction");
			Category romance = new Category("Romance");
			Category horror = new Category("Horror");
			Category adventure = new Category("Adventure");
			crepository.save(thriller);
			crepository.save(sciFi);
			crepository.save(romance);
			crepository.save(horror);
			crepository.save(adventure);

			Book book1 = new Book("Great Gatsby", "Scott Fitzgerald", "123GPA123", 1925, 10.9,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fddd.webp?alt=media&token=d7f8f06b-86a3-41b9-a5d5-5e05ae1858ad");
			Book book2 = new Book("451 Fahrenheit", "Ray Bradbury", "123GPA222", 1951, 5.95,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fss.jfif?alt=media&token=316a351a-04bc-4f73-9a32-b8a44da9cf05");
			Book book3 = new Book("It", "Steven King", "123GPA223", 1986, 12.5,
					horror,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fit.webp2320d8f8-7a30-4a76-9118-006c4a2e2463?alt=media&token=23901e7e-7b89-4803-82b5-ad9f47c6bb4d");
			Book book4 = new Book("Fight Club", "Chuck Palahniuk", "123GPA323", 1996, 10.5,
					thriller,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2F36236124.jpgdc780c5b-ccb2-46e8-8d7d-d7101bc05bf9?alt=media&token=e94d48ef-ac19-486f-a099-03c5744ffda4");
			Book book5 = new Book("Tender is the Night", "Scott Fitzgerald", "123GPA423", 1934, 12.5,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Ftendernight.webp?alt=media&token=54cba93b-7130-45b5-a8f5-78584deb67d0");
			Book book6 = new Book("Harry Potter and the Chamber of Secrets", "Joanne Rowling", "123GPA523", 1998, 8.5,
					adventure,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fharry2.webp?alt=media&token=2475ad48-1160-40b0-813b-88ed1fa0b25f");
			Book book7 = new Book("Harry Potter and the Philosopher's Stone", "Joanne Rowling", "123GPA623", 1997, 7.5,
					adventure,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fharry1.webp?alt=media&token=5d844012-206f-49c3-9174-268d4ac9961e");
			Book book8 = new Book("Harry Potter and the Prisoner of Azkaban", "Joanne Rowling", "123GPA723", 1999, 9.5,
					adventure,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fharry3.webp?alt=media&token=f2bfea53-6eba-44e0-9334-001784dc6876");
			Book book9 = new Book("Harry Potter and the Goblet of Fire", "Joanne Rowling", "123GPA823", 2000, 10.5,
					adventure,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fharry4.webp?alt=media&token=f2975a67-7f54-4923-99f9-9c345da7e490");
			Book book10 = new Book("Harry Potter and the Order of the Phoenix", "Joanne Rowling", "123GPA923", 2003,
					11.5,
					adventure,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fharry5.webp?alt=media&token=ec85d5ee-22cb-4119-b0b8-1a37ef79bf13");
			Book book11 = new Book("Harry Potter and Half-Blood Prince", "Joanne Rowling", "123GPA233", 2005, 12.5,
					adventure,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fharry6.webp?alt=media&token=b9917dc5-5b49-463c-8a79-83aaeb9087f7");
			Book book12 = new Book("Harry Potter and the Deathly Hallows", "Joanne Rowling", "123GPA243", 2007, 20.5,
					adventure,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fharry7.webp?alt=media&token=1424278e-079d-4888-951f-f6421b35c761");
			Book book13 = new Book("Anna Karenina", "Leo Tolstoy", "123GPA253", 1877, 13,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fkarenina.webp?alt=media&token=a71c78a1-a6fa-4ff5-9d55-cfac6437038d");
			Book book14 = new Book("For Whom The Bell Tolls", "Ernest Hemingway", "123GPA263", 1940, 10.5,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Ffor%20whom%20the%20bell%20tolls.webp?alt=media&token=673bb17e-1780-4db9-a084-da217fcf4445");
			Book book15 = new Book("Death in the Afternoon", "Ernest Hemingway", "123GPA273", 1932, 9.75,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fdeath.webp?alt=media&token=c1e8c6b3-a583-4cd0-8e31-77aa74388574");
			Book book16 = new Book("Romeo and Juliet", "William Shakespeare", "123GPA283", 1597, 20,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fromeo.webp?alt=media&token=f0846463-3a3e-4126-a75e-fe9307d80317");
			Book book17 = new Book("The Brothers Karamazov", "Fyodor Dostoevsky", "123GPA293", 1880, 21.25,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fbrothers.webp?alt=media&token=e7459e75-da02-4f83-bb41-f334f465c53f");
			Book book18 = new Book("The Meek One", "Fyodor Dostoevsky", "123GPA333", 1876, 7.52,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2FThe%20meek.webp?alt=media&token=52d4787a-0e1e-4b9c-9801-e80ee85a5b40");
			Book book19 = new Book("Crime And Punishment", "Fyodor Dostoevsky", "123DPA923", 1866, 40.5,
					thriller,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fcrime.jpg?alt=media&token=f1d9fb02-5fc0-4c56-9b73-7d1d7b1da101");
			Book book20 = new Book("Ugly Love", "Colleen Hoover", "123DPA233", 2014, 32.5,
					thriller,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fugly%20love.jpg?alt=media&token=5c7641bc-788f-49c9-b70e-92ff2534e4a4");
			Book book21 = new Book("One Of Us Is Lying", "Karen M. McManus", "123DPA243", 2017, 24.5,
					thriller,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fone-of-us-is-lying.jpg?alt=media&token=b7e8272d-3c46-4cf7-95ca-03e06978311e");
			Book book22 = new Book("The Alchemist", "Paulo Coelho", "123DPA253", 1988, 14,
					sciFi,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fthe-alchemist-25th-anniversary-edition.jpg?alt=media&token=62fa5d1e-15c0-4327-9806-0fbe9ecf5ae9");
			Book book23 = new Book("Great Dune", "Frank Herbert", "123DPA263", 1979, 23.5,
					sciFi,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fgreat-dune-trilogy.jpg?alt=media&token=8e0a59c5-af91-4616-ae3a-d1285582e577");
			Book book24 = new Book("1984", "George Orwell", "123DPA273", 1949, 44.75,
					sciFi,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2F1984.jpg?alt=media&token=a07c55e7-9a0f-45df-ae8b-7bfb3fef6987");
			Book book25 = new Book("Animal Farm", "George Orwell", "123DPA283", 1945, 10.32,
					sciFi,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2FThe%20meek.webp?alt=media&token=52d4787a-0e1e-4b9c-9801-e80ee85a5b40");
			Book book26 = new Book("Erich Remarque", "Three Comrades", "123DPA293", 1938, 5.25,
					romance,
					"https://firebasestorage.googleapis.com/v0/b/mytest-585af.appspot.com/o/covers%2Fremark.webp?alt=media&token=6c76d008-3b71-4e34-a242-5fa54c3592ce");

			repository.save(book1);
			repository.save(book2);
			repository.save(book3);
			repository.save(book4);
			repository.save(book5);
			repository.save(book6);
			repository.save(book7);
			repository.save(book8);
			repository.save(book9);
			repository.save(book10);
			repository.save(book11);
			repository.save(book12);
			repository.save(book13);
			repository.save(book14);
			repository.save(book15);
			repository.save(book16);
			repository.save(book17);
			repository.save(book18);
			repository.save(book19);
			repository.save(book20);
			repository.save(book21);
			repository.save(book22);
			repository.save(book23);
			repository.save(book24);
			repository.save(book25);
			repository.save(book26);

			String password = "test";
			BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
			String hashPwd = bc.encode(password);

			User unverifiedUser = new User("Firstname", "Lastname", "userunver", hashPwd, "USER", "mymaild@gmail.com",
					"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@", false);

			urepository.save(unverifiedUser);

			User newUser1 = new User("First", "Userok", "user", hashPwd, "USER", "mymail@gmail.com", true);
			User newUser2 = new User("Second", "userRole", "user2", hashPwd, "USER", "mymail232@gmail.com", true);
			User newAdmin = new User("First", "Admin", "admin", hashPwd, "ADMIN", "mymail2@gmail.com", true);

			urepository.save(newUser1);
			urepository.save(newAdmin);
			urepository.save(newUser2);

			Cart currentCartUser1 = new Cart(true, newUser1);
			Cart currentCartUser2 = new Cart(true, newUser2);
			Cart currentCartAdmin = new Cart(true, newAdmin);
			barepository.save(currentCartUser1);
			barepository.save(currentCartUser2);
			barepository.save(currentCartAdmin);

			Cart cart1User1 = new Cart(false, newUser1);
			Cart cart1Admin = new Cart(false, newAdmin);
			Cart cart2Admin = new Cart(false, newAdmin);

			barepository.save(cart1User1);
			barepository.save(cart1Admin);
			barepository.save(cart2Admin);

			bbrepository.save(new CartBook(3, cart1User1, book1));
			bbrepository.save(new CartBook(3, cart1User1, book2));
			bbrepository.save(new CartBook(2, cart1User1, book3));
			bbrepository.save(new CartBook(1, cart1User1, book4));
			bbrepository.save(new CartBook(3, cart1User1, book5));
			bbrepository.save(new CartBook(2, cart1User1, book6));
			bbrepository.save(new CartBook(1, cart1User1, book7));
			bbrepository.save(new CartBook(4, cart1User1, book8));
			bbrepository.save(new CartBook(3, cart1User1, book9));
			bbrepository.save(new CartBook(4, cart1User1, book10));

			bbrepository.save(new CartBook(1, cart1Admin, book2));
			bbrepository.save(new CartBook(3, cart1Admin, book11));
			bbrepository.save(new CartBook(2, cart1Admin, book4));
			bbrepository.save(new CartBook(1, cart1Admin, book5));
			bbrepository.save(new CartBook(3, cart1Admin, book7));
			bbrepository.save(new CartBook(2, cart1Admin, book8));
			bbrepository.save(new CartBook(1, cart1Admin, book9));
			bbrepository.save(new CartBook(4, cart1Admin, book10));
			bbrepository.save(new CartBook(3, cart1Admin, book12));
			bbrepository.save(new CartBook(4, cart1Admin, book13));
			bbrepository.save(new CartBook(1, cart1Admin, book14));
			bbrepository.save(new CartBook(2, cart1Admin, book15));

			bbrepository.save(new CartBook(1, cart2Admin, book14));
			bbrepository.save(new CartBook(2, cart2Admin, book15));

			orepository.save(new Order("First", "Admin", "Finland", "Helsinki", "Juustenintie 3J 110", "00410",
					"In progress", "mymail@mail.com", cart1Admin, hashPwd));
			orepository.save(new Order("First", "Userok", "Jiji", "Hur", "Mesti 28 177", "511120", "In progress",
					"mymail2@mail.com", cart1User1, hashPwd));
			orepository.save(new Order("Jessie", "Sun", "US", "Berkeley", "Haste 2112", "94705",
					"jessiesun@example.com", cart2Admin, "Make my order quick please", hashPwd));
		};
	}
}
