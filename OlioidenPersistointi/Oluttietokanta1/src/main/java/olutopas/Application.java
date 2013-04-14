package olutopas;

import com.avaje.ebean.EbeanServer;
import java.util.List;
import java.util.Scanner;
import javax.persistence.OptimisticLockException;
import olutopas.model.Beer;
import olutopas.model.Brewery;
import olutopas.model.Rating;
import olutopas.model.User;

public class Application {

    private EbeanServer server;
    private Scanner scanner = new Scanner(System.in);
    private User kayttaja;

    public Application(EbeanServer server) {
        this.server = server;
    }

    public void run(boolean newDatabase) {
        if (newDatabase) {
            seedDatabase();
        }

        System.out.println("Welcome!");

        while (true) {
            System.out.println("Login (give ? to register a new user)");
            System.out.print("username: ");
            String name = scanner.nextLine();
            User user;
            if (name.equals("?")) {
                System.out.println("");
                System.out.println("Reqister a new user");
                System.out.print("give username: ");
                String newName = scanner.nextLine();
                user = server.find(User.class).where().like("name", newName).findUnique();

                if (user != null) {
                    continue;
                }
                User newUser = new User(newName);
                server.save(newUser);
                System.out.println("user created!");
                kayttaja = newUser;
                break;

            } else {
                user = server.find(User.class).where().like("name", name).findUnique();
                if (user == null) {
                    System.out.println("wrong username, please try again!");
                    System.out.println("");
                    continue;
                }
                kayttaja = user;
                break;
            }
        }

        while (true) {
            menu();
            System.out.print("> ");
            String command = scanner.nextLine();

            if (command.equals("0")) {
                break;
            } else if (command.equals("1")) {
                findBeer();
            } else if (command.equals("2")) {
                findBrewery();
            } else if (command.equals("3")) {
                addBeer();
            } else if (command.equals("4")) {
                addBrewery();
            } else if (command.equals("5")) {
                listBeers();
            } else if (command.equals("6")) {
                listBreweries();
            } else if (command.equals("7")) {
                deleteBeer();
            } else if (command.equals("8")) {
                deleteBrewery();
            } else if (command.equals("l")) {
                listUsers();
            } else if (command.equals("t")) {
                showMyRatings();
            } else {
                System.out.println("unknown command");
            }

            System.out.print("\npress enter to continue");
            scanner.nextLine();
        }

        System.out.println("bye");
    }

    private void menu() {
        System.out.println("");
        System.out.println("1   find/rate beer");
        System.out.println("2   find brewery");
        System.out.println("3   add beer");
        System.out.println("4   add brewery");
        System.out.println("5   list beers");
        System.out.println("6   list breweries");
        System.out.println("7   delete beer");
        System.out.println("8   delete brewery");
        System.out.println("l   list users");
        System.out.println("t   show my ratings");
        System.out.println("0   quit");
        System.out.println("");
    }

    // jos kanta on luotu uudelleen, suoritetaan tämä ja laitetaan kantaan hiukan dataa
    private void seedDatabase() throws OptimisticLockException {
        User user = new User("Matti");
        server.save(user);
        Brewery brewery = new Brewery("Schlenkerla");
        brewery.addBeer(new Beer("Urbock"));
        brewery.addBeer(new Beer("Lager"));
        // tallettaa myös luodut oluet, sillä Brewery:n OneToMany-mappingiin on määritelty
        // CascadeType.all
        server.save(brewery);

        // luodaan olut ilman panimon asettamista
        Beer b = new Beer("Märzen");
        server.save(b);

        // jotta saamme panimon asetettua, tulee olot lukea uudelleen kannasta
        b = server.find(Beer.class, b.getId());
        brewery = server.find(Brewery.class, brewery.getId());
        brewery.addBeer(b);
        server.save(brewery);
        Rating rate = new Rating(b, user, 3);
        server.save(rate);

        server.save(new Brewery("Paulaner"));
    }

    private void findBeer() {
        System.out.print("beer to find: ");
        String n = scanner.nextLine();
        Beer foundBeer = server.find(Beer.class).where().like("name", n).findUnique();

        if (foundBeer == null) {
            System.out.println(n + " not found");
            return;
        }

        System.out.print("found: " + foundBeer);



        System.out.println("Average Rating: " + beerAverage(foundBeer));
        System.out.println("  Rate beer from 1 to 5 ('0' if you don't want to rate)");
        int value = scanner.nextInt();
        if (value > 0) {
            Rating rate = new Rating(foundBeer, kayttaja, value);
            server.save(rate);
        }
        System.out.println(foundBeer + "rated " + value + " by " + kayttaja);
    }

    private Double beerAverage(Beer beer) {
        List<Rating> lt = listRatings();
        int summa = 0;
        int count = 0;

        for (Rating rating : lt) {
            if (rating.getBeer().getId() == beer.getId()) {
                summa = summa + rating.getRate();
                count++;
            } else {
            }
        }
        if (count == 0) {
            return (double) 0;
        } else {
            return (double) summa / (double) count;
        }
    }
    
    private void showMyRatings(){
        List<Rating> lt = listRatings();
        for (Rating rating : lt) {
            if (rating.getUser().getId() == kayttaja.getId()){
                System.out.println(rating.getBeer() + " " + rating.getRate());
            }
            
        }
    }

    private void findBrewery() {
        System.out.print("brewery to find: ");
        String n = scanner.nextLine();
        Brewery foundBrewery = server.find(Brewery.class).where().like("name", n).findUnique();

        if (foundBrewery == null) {
            System.out.println(n + " not found");
            return;
        }

        System.out.println(foundBrewery);
        for (Beer bier : foundBrewery.getBeers()) {
            System.out.println("   " + bier.getName());
        }
    }

    private List<Rating> listRatings() {
        return server.find(Rating.class).findList();
    }

    private void listBeers() {
        List<Beer> beers = server.find(Beer.class).findList();
        for (Beer beer : beers) {
            System.out.println(beer + " " + beerAverage(beer));
        }
    }

    private void listUsers() {
        List<User> users = server.find(User.class).findList();
        for (User user : users) {
            System.out.println(user);
        }
    }

    private void listBreweries() {
        List<Brewery> breweries = server.find(Brewery.class).findList();
        for (Brewery brewery : breweries) {
            System.out.println(brewery);
        }
    }

    private void addBeer() {
        System.out.print("to which brewery: ");
        String name = scanner.nextLine();
        Brewery brewery = server.find(Brewery.class).where().like("name", name).findUnique();

        if (brewery == null) {
            System.out.println(name + " does not exist");
            return;
        }

        System.out.print("beer to add: ");

        name = scanner.nextLine();

        Beer exists = server.find(Beer.class).where().like("name", name).findUnique();
        if (exists != null) {
            System.out.println(name + " exists already");
            return;
        }

        brewery.addBeer(new Beer(name));
        server.save(brewery);
        System.out.println(name + " added to " + brewery.getName());
    }

    private void addBrewery() {

        System.out.print("beer to add: ");
        String name = scanner.nextLine();
        Brewery brewery = server.find(Brewery.class).where().like("name", name).findUnique();
        if (brewery != null) {
            return;
        }

        Brewery newBrewery = new Brewery(name);
        server.save(newBrewery);


    }

    private void deleteBeer() {
        System.out.print("beer to delete: ");
        String n = scanner.nextLine();
        Beer beerToDelete = server.find(Beer.class).where().like("name", n).findUnique();

        if (beerToDelete == null) {
            System.out.println(n + " not found");
            return;
        }

        server.delete(beerToDelete);
        System.out.println("deleted: " + beerToDelete);

    }

    private void deleteBrewery() {
        System.out.print("brewery to delete: ");
        String n = scanner.nextLine();
        Brewery breweryToDelete = server.find(Brewery.class).where().like("name", n).findUnique();

        if (breweryToDelete == null) {
            System.out.println(n + " not found");
            return;
        }
        server.delete(breweryToDelete);
        System.out.println("deleted: " + breweryToDelete);

    }
}
