import java.io.*;
import java.util.*;

/**
 * This class runs a simple simulation of animals in a field.
 * It reads input data from a file, creates animals, simulates their behavior over a number of days,
 * and then prints out the results.
 */
public class AnimalsInField {

    /**
     * An enumeration representing different animal sounds.
     * Each animal type can produce a specific sound.
     */
    public enum AnimalSound {
        LION("Roar"),
        ZEBRA("Ihoho"),
        BOAR("Oink");

        private final String sound;

        /**
         * Creates a new AnimalSound with the given sound.
         * @param sound The sound that this animal makes.
         */
        AnimalSound(String sound) {
            this.sound = sound;
        }

        /**
         * Returns the sound of this animal.
         * @return The animal's sound as a string.
         */
        public String getSound() {
            return sound;
        }
    }

    /**
     * The Carnivore interface should be implemented by animals that eat other animals.
     */
    public interface Carnivore {
        /**
         * Chooses a prey animal from a list of animals.
         * @param animals The list of all animals.
         * @param hunter The animal trying to find a prey.
         * @return The chosen prey animal, or null if no valid prey is found.
         * @throws CannibalismException If the hunter tries to eat the same type of animal as itself.
         * @throws TooStrongPreyException If the chosen prey is too strong or too fast.
         * @throws SelfHuntingException If the hunter tries to hunt itself.
         */
        Animal choosePrey(List<Animal> animals, Animal hunter) throws CannibalismException,
                TooStrongPreyException, SelfHuntingException;

        /**
         * Hunts and eats the chosen prey, increasing the hunter's energy.
         * @param hunter The animal doing the hunting.
         * @param prey The animal being hunted.
         */
        void huntPrey(Animal hunter, Animal prey);
    }

    /**
     * The Herbivore interface should be implemented by animals that eat plants (grass).
     */
    public interface Herbivore {
        /**
         * Makes the animal eat grass from the field.
         * This increases the animal's energy and decreases the grass amount in the field.
         * @param grazer The animal eating the grass.
         * @param field The field with the grass.
         */
        void grazeInField(Animal grazer, Field field);
    }

    /**
     * The Omnivore interface is for animals that can eat both plants and meat.
     * It extends both Carnivore and Herbivore.
     */
    public interface Omnivore extends Carnivore, Herbivore {
    }

    /**
     * The Field class represents a piece of land that grows grass.
     * Animals can eat the grass, and the grass can regrow.
     */
    public static class Field {
        /**
         * The maximum amount of grass the field can hold.
         */
        public static final float MAX_GRASS_AMOUNT = 100f;
        /**
         * The minimum amount of grass (usually zero).
         */
        public static final float MIN_GRASS_AMOUNT = 0f;
        private float grassAmount;

        /**
         * Creates a field with a certain amount of grass.
         * @param grassAmount The starting amount of grass.
         * @throws GrassOutOfBoundsException If the grass amount is less than MIN_GRASS_AMOUNT
         * or more than MAX_GRASS_AMOUNT.
         */
        public Field(float grassAmount) throws GrassOutOfBoundsException {
            if (grassAmount < MIN_GRASS_AMOUNT || grassAmount > MAX_GRASS_AMOUNT) {
                throw new GrassOutOfBoundsException();
            }
            this.grassAmount = grassAmount;
        }

        /**
         * Returns the current amount of grass in the field.
         * @return The amount of grass.
         */
        public float getGrassAmount() {
            return grassAmount;
        }

        /**
         * Sets the grass amount in the field.
         * This will not let grass go below the minimum or above the maximum.
         * @param grassAmount The new amount of grass.
         */
        public void setGrassAmount(float grassAmount) {
            this.grassAmount = Math.min(Math.max(grassAmount, MIN_GRASS_AMOUNT), MAX_GRASS_AMOUNT);
        }

        /**
         * Makes the grass grow by doubling it, but not beyond the maximum limit.
         */
        public void makeGrassGrow() {
            grassAmount *= 2;
            if (grassAmount > MAX_GRASS_AMOUNT) {
                grassAmount = MAX_GRASS_AMOUNT;
            }
        }
    }

    /**
     * The Animal class is an abstract class that represents any animal.
     * It has basic attributes like sound, weight, speed, and energy.
     * Concrete animal types (like Lion, Zebra, Boar) extend this class.
     */
    public abstract static class Animal {
        /**
         * The smallest allowed weight for any animal.
         */
        public static final float MIN_WEIGHT = 5f;
        /**
         * The largest allowed weight for any animal.
         */
        public static final float MAX_WEIGHT = 200f;
        /**
         * The slowest speed allowed for any animal.
         */
        public static final float MIN_SPEED = 5f;
        /**
         * The fastest speed allowed for any animal.
         */
        public static final float MAX_SPEED = 60f;
        /**
         * The lowest energy level allowed (0 means dead).
         */
        public static final float MIN_ENERGY = 0f;
        /**
         * The highest energy level allowed.
         */
        public static final float MAX_ENERGY = 100f;

        protected AnimalSound sound;
        protected float weight;
        protected float speed;
        protected float energy;

        /**
         * Creates a new Animal with the given properties.
         * @param sound The sound the animal makes.
         * @param weight How heavy the animal is.
         * @param speed How fast the animal can run.
         * @param energy The current energy level of the animal.
         * @throws WeightOutOfBoundsException If weight is not within allowed limits.
         * @throws SpeedOutOfBoundsException If speed is not within allowed limits.
         * @throws EnergyOutOfBoundsException If energy is not within allowed limits.
         */
        public Animal(AnimalSound sound, float weight, float speed, float energy) throws WeightOutOfBoundsException,
                SpeedOutOfBoundsException, EnergyOutOfBoundsException {
            if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
                throw new WeightOutOfBoundsException();
            }
            if (speed < MIN_SPEED || speed > MAX_SPEED) {
                throw new SpeedOutOfBoundsException();
            }
            if (energy < MIN_ENERGY || energy > MAX_ENERGY) {
                throw new EnergyOutOfBoundsException();
            }
            this.sound = sound;
            this.weight = weight;
            this.speed = speed;
            this.energy = energy;
        }

        /**
         * Makes the animal produce its sound, but only if it is alive.
         */
        public abstract void makeSound();

        /**
         * Lowers the animal's energy by 1% of its current energy.
         * If energy drops below 0, it is set to 0.
         */
        public void decrementEnergy() {
            energy -= energy * 0.01f;
            if (energy < MIN_ENERGY) {
                energy = MIN_ENERGY;
            }
        }

        /**
         * Makes the animal eat something, depending on its type.
         * Carnivores will try to eat other animals.
         * Herbivores will eat grass.
         * Omnivores will do both.
         * @param animals The list of all animals.
         * @param field The field with grass.
         * @throws CannibalismException If the animal tries to eat one of its own kind.
         * @throws TooStrongPreyException If the chosen prey is too powerful.
         * @throws SelfHuntingException If the animal tries to hunt itself.
         */
        public abstract void eat(List<Animal> animals, Field field) throws CannibalismException,
                TooStrongPreyException, SelfHuntingException;

        /**
         * Checks if the animal is still alive.
         * An animal is alive if its energy is greater than 0.
         * @return True if alive, False if dead.
         */
        public boolean isAlive() {
            return energy > 0;
        }

        /**
         * Returns the weight of the animal.
         * @return The animal's weight.
         */
        public float getWeight() {
            return weight;
        }

        /**
         * Returns the speed of the animal.
         * @return The animal's speed.
         */
        public float getSpeed() {
            return speed;
        }

        /**
         * Returns the energy of the animal.
         * @return The animal's current energy level.
         */
        public float getEnergy() {
            return energy;
        }

        /**
         * Sets the animal's energy to a new value, not exceeding the max energy or dropping below min energy.
         * @param energy The new energy level.
         */
        public void setEnergy(float energy) {
            this.energy = Math.min(energy, MAX_ENERGY);
            if (this.energy < MIN_ENERGY) {
                this.energy = MIN_ENERGY;
            }
        }
    }

    /**
     * The Lion class represents a lion, which is a carnivore.
     * Lions hunt other animals to gain energy.
     */
    public static class Lion extends Animal implements Carnivore {
        /**
         * Creates a new Lion with given attributes.
         */
        public Lion(float weight, float speed, float energy) throws WeightOutOfBoundsException,
                SpeedOutOfBoundsException, EnergyOutOfBoundsException {
            super(AnimalSound.LION, weight, speed, energy);
        }

        /**
         * Makes the lion roar if it is alive.
         */
        @Override
        public void makeSound() {
            if (isAlive()) {
                System.out.println(sound.getSound());
            }
        }

        /**
         * The lion tries to find prey and eat it.
         * @throws CannibalismException If the lion tries to eat another lion.
         * @throws TooStrongPreyException If the chosen prey is too strong.
         * @throws SelfHuntingException If the lion tries to eat itself.
         */
        @Override
        public void eat(List<Animal> animals, Field field) throws CannibalismException,
                TooStrongPreyException, SelfHuntingException {
            Animal prey = choosePrey(animals, this);
            if (prey == null) {
                return;
            }
            if (prey.isAlive()) {
                huntPrey(this, prey);
            }
        }

        /**
         * Chooses a prey animal by looking at the next animal in the list.
         */
        @Override
        public Animal choosePrey(List<Animal> animals, Animal hunter) throws CannibalismException,
                TooStrongPreyException, SelfHuntingException {
            int index = animals.indexOf(hunter);
            int preyIndex = (index + 1) % animals.size();
            Animal prey = animals.get(preyIndex);

            if (hunter == prey) {
                throw new SelfHuntingException();
            }
            if (!prey.isAlive()) {
                return null;
            }
            if (prey.sound == hunter.sound) {
                throw new CannibalismException();
            }
            if (prey.getSpeed() > hunter.getSpeed() && prey.getEnergy() > hunter.getEnergy()) {
                throw new TooStrongPreyException();
            }

            return prey;
        }

        /**
         * Hunts the chosen prey, setting the prey's energy to zero (killing it),
         * and increasing the lion's energy by the prey's weight.
         */
        @Override
        public void huntPrey(Animal hunter, Animal prey) {
            prey.setEnergy(0f);
            hunter.setEnergy(hunter.getEnergy() + prey.getWeight());
        }
    }

    /**
     * The Zebra class represents a zebra, which is a herbivore.
     * Zebras eat grass from the field.
     */
    public static class Zebra extends Animal implements Herbivore {
        /**
         * Creates a new Zebra with given attributes.
         */
        public Zebra(float weight, float speed, float energy) throws WeightOutOfBoundsException,
                SpeedOutOfBoundsException, EnergyOutOfBoundsException {
            super(AnimalSound.ZEBRA, weight, speed, energy);
        }

        /**
         * Makes the zebra produce its sound if it is alive.
         */
        @Override
        public void makeSound() {
            if (isAlive()) {
                System.out.println(sound.getSound());
            }
        }

        /**
         * Zebras only eat grass, so this calls grazeInField.
         */
        @Override
        public void eat(List<Animal> animals, Field field) {
            grazeInField(this, field);
        }

        /**
         * Zebras eat grass from the field.
         * They use their weight to figure out how much grass they need.
         */
        @Override
        public void grazeInField(Animal grazer, Field field) {
            float requiredGrass = grazer.getWeight() / 10f;
            if (field.getGrassAmount() > requiredGrass) {
                setEnergy(getEnergy() + requiredGrass);
                field.setGrassAmount(field.getGrassAmount() - requiredGrass);
            }
        }
    }

    /**
     * The Boar class represents a boar, which is an omnivore.
     * Boars eat both grass and can hunt other animals.
     */
    public static class Boar extends Animal implements Omnivore {
        /**
         * Creates a new Boar with given attributes.
         */
        public Boar(float weight, float speed, float energy) throws WeightOutOfBoundsException,
                SpeedOutOfBoundsException, EnergyOutOfBoundsException {
            super(AnimalSound.BOAR, weight, speed, energy);
        }

        /**
         * Makes the boar produce its sound if it is alive.
         */
        @Override
        public void makeSound() {
            if (isAlive()) {
                System.out.println(sound.getSound());
            }
        }

        /**
         * Boars first eat grass, then try to hunt another animal if possible.
         * @throws CannibalismException If the boar tries to eat another boar.
         * @throws TooStrongPreyException If the chosen prey is too strong.
         * @throws SelfHuntingException If the boar tries to eat itself.
         */
        @Override
        public void eat(List<Animal> animals, Field field) throws CannibalismException,
                TooStrongPreyException, SelfHuntingException {
            grazeInField(this, field);
            Animal prey = choosePrey(animals, this);
            if (prey == null) {
                return;
            }
            if (prey.isAlive()) {
                huntPrey(this, prey);
            }
        }

        /**
         * Boars eat grass just like zebras.
         */
        @Override
        public void grazeInField(Animal grazer, Field field) {
            float requiredGrass = grazer.getWeight() / 10f;
            if (field.getGrassAmount() > requiredGrass) {
                setEnergy(getEnergy() + requiredGrass);
                field.setGrassAmount(field.getGrassAmount() - requiredGrass);
            }
        }

        /**
         * Chooses a prey animal similarly to the lion.
         */
        @Override
        public Animal choosePrey(List<Animal> animals, Animal hunter) throws CannibalismException,
                TooStrongPreyException, SelfHuntingException {
            int index = animals.indexOf(hunter);
            int preyIndex = (index + 1) % animals.size();
            Animal prey = animals.get(preyIndex);

            if (hunter == prey) {
                throw new SelfHuntingException();
            }
            if (!prey.isAlive()) {
                return null;
            }
            if (prey.sound == hunter.sound) {
                throw new CannibalismException();
            }
            if (prey.getSpeed() > hunter.getSpeed() && prey.getEnergy() > hunter.getEnergy()) {
                throw new TooStrongPreyException();
            }

            return prey;
        }

        /**
         * Hunts the prey like the lion does, killing it and gaining energy.
         */
        @Override
        public void huntPrey(Animal hunter, Animal prey) {
            prey.setEnergy(0f);
            hunter.setEnergy(hunter.getEnergy() + prey.getWeight());
        }
    }

    /**
     * This exception is thrown if the grass amount goes beyond allowed limits.
     */
    public static class GrassOutOfBoundsException extends Exception {
        @Override
        public String getMessage() {
            return "The grass is out of bounds";
        }
    }

    /**
     * This exception is thrown if the number of animal parameters is not correct.
     */
    public static class InvalidNumberOfAnimalParametersException extends Exception {
        @Override
        public String getMessage() {
            return "Invalid number of animal parameters";
        }
    }

    /**
     * This exception is thrown for other invalid inputs (like invalid day count or animal type).
     */
    public static class InvalidInputsException extends Exception {
        @Override
        public String getMessage() {
            return "Invalid inputs";
        }
    }

    /**
     * This exception is thrown if the animal's weight is not within the allowed range.
     */
    public static class WeightOutOfBoundsException extends Exception {
        @Override
        public String getMessage() {
            return "The weight is out of bounds";
        }
    }

    /**
     * This exception is thrown if the animal's speed is not within the allowed range.
     */
    public static class SpeedOutOfBoundsException extends Exception {
        @Override
        public String getMessage() {
            return "The speed is out of bounds";
        }
    }

    /**
     * This exception is thrown if the animal's energy is not within the allowed range.
     */
    public static class EnergyOutOfBoundsException extends Exception {
        @Override
        public String getMessage() {
            return "The energy is out of bounds";
        }
    }

    /**
     * This exception is thrown if a carnivore or omnivore tries to eat another animal of the same type (cannibalism).
     */
    public static class CannibalismException extends Exception {
        @Override
        public String getMessage() {
            return "Cannibalism is not allowed";
        }
    }

    /**
     * This exception is thrown if a chosen prey is too strong or too fast for the hunter.
     */
    public static class TooStrongPreyException extends Exception {
        @Override
        public String getMessage() {
            return "The prey is too strong or too fast to attack";
        }
    }

    /**
     * This exception is thrown if an animal tries to hunt itself.
     */
    public static class SelfHuntingException extends Exception {
        @Override
        public String getMessage() {
            return "Self-hunting is not allowed";
        }
    }

    /**
     * Parses a string to a float, allowing it to end with an 'F' or 'f'.
     * @param str The string to parse.
     * @return The float value.
     * @throws NumberFormatException If the string is not a proper number.
     */
    public static float parseFloatWithSuffix(String str) throws NumberFormatException {
        if (str.endsWith("F") || str.endsWith("f")) {
            str = str.substring(0, str.length() - 1);
        }
        return Float.parseFloat(str);
    }

    /**
     * Checks if the given number of days is valid (1 to 30).
     * @param dLine The days as a string.
     * @return The valid number of days.
     * @throws InvalidInputsException If the days are not in the valid range.
     */
    private static int validateDays(String dLine) throws InvalidInputsException {
        try {
            int days = Integer.parseInt(dLine);
            if (days < 1 || days > 30) {
                throw new InvalidInputsException();
            }
            return days;
        } catch (Exception e) {
            throw new InvalidInputsException();
        }
    }

    /**
     * Checks if the grass amount is valid (0 to 100).
     * @param grassLine The grass amount as a string.
     * @return The valid grass amount.
     * @throws GrassOutOfBoundsException If the amount is outside the allowed range.
     */
    private static float validateGrassAmount(String grassLine) throws GrassOutOfBoundsException {
        try {
            float grassAmount = parseFloatWithSuffix(grassLine);
            if (grassAmount < 0 || grassAmount > 100) {
                throw new GrassOutOfBoundsException();
            }
            return grassAmount;
        } catch (Exception e) {
            throw new GrassOutOfBoundsException();
        }
    }

    /**
     * Checks if the number of animals is valid (1 to 20).
     * @param nLine The number of animals as a string.
     * @return The valid number of animals.
     * @throws InvalidInputsException If the number is not in the valid range.
     */
    private static int validateNumberOfAnimals(String nLine) throws InvalidInputsException {
        try {
            int numberOfAnimals = Integer.parseInt(nLine);
            if (numberOfAnimals < 1 || numberOfAnimals > 20) {
                throw new InvalidInputsException();
            }
            return numberOfAnimals;
        } catch (Exception e) {
            throw new InvalidInputsException();
        }
    }

    /**
     * Checks and creates an animal from a line of input.
     * The line should have: type, weight, speed, energy.
     * @param line The input line.
     * @return A new Animal object.
     * @throws Exception If any of the parameters are invalid.
     */
    private static Animal validateAndCreateAnimal(String line) throws Exception {
        String[] tokens = line.split("\\s+");
        if (tokens.length != 4) {
            throw new InvalidNumberOfAnimalParametersException();
        }

        String type = tokens[0];
        float weight;
        float speed;
        float energy;

        try {
            weight = parseFloatWithSuffix(tokens[1]);
            speed = parseFloatWithSuffix(tokens[2]);
            energy = parseFloatWithSuffix(tokens[3]);
        } catch (NumberFormatException e) {
            throw new InvalidInputsException();
        }

        if (!type.equals("Lion") && !type.equals("Zebra") && !type.equals("Boar")) {
            throw new InvalidInputsException();
        }

        if (weight < Animal.MIN_WEIGHT || weight > Animal.MAX_WEIGHT) {
            throw new WeightOutOfBoundsException();
        }
        if (speed < Animal.MIN_SPEED || speed > Animal.MAX_SPEED) {
            throw new SpeedOutOfBoundsException();
        }
        if (energy < Animal.MIN_ENERGY || energy > Animal.MAX_ENERGY) {
            throw new EnergyOutOfBoundsException();
        }

        switch (type) {
            case "Lion":
                return new Lion(weight, speed, energy);
            case "Zebra":
                return new Zebra(weight, speed, energy);
            case "Boar":
                return new Boar(weight, speed, energy);
            default:
                throw new InvalidInputsException();
        }
    }

    /**
     * Runs the main simulation.
     * For a given number of days, each animal will try to eat.
     * After that, each animal loses some energy.
     * Dead animals are removed from the list.
     * Grass grows back each day.
     * @param days How many days to simulate.
     * @param grassAmount Starting amount of grass in the field.
     * @param animals The list of animals.
     * @throws GrassOutOfBoundsException If grass is invalid at start.
     */
    public static void runSimulation(int days, float grassAmount,
                                     List<Animal> animals) throws GrassOutOfBoundsException {
        Field field = new Field(grassAmount);

        for (int day = 0; day < days; day++) {
            for (Animal animal : new ArrayList<>(animals)) {
                if (!animal.isAlive()) {
                    continue;
                }
                try {
                    animal.eat(animals, field);
                } catch (SelfHuntingException | CannibalismException | TooStrongPreyException e) {
                    System.out.println(e.getMessage());
                }
            }

            for (Animal animal : animals) {
                animal.decrementEnergy();
            }

            removeDeadAnimals(animals);
            field.makeGrassGrow();
        }
    }

    /**
     * Prints the sounds of all surviving animals.
     */
    public static void printAnimals(List<Animal> animals) {
        for (Animal animal : animals) {
            if (animal.isAlive()) {
                animal.makeSound();
            }
        }
    }

    /**
     * Removes all animals that have 0 or less energy (dead animals).
     */
    public static void removeDeadAnimals(List<Animal> animals) {
        animals.removeIf(a -> !a.isAlive());
    }

    /**
     * The main method reads input data, runs the simulation, and then prints the results.
     */
    public static void main(String[] args) {
        List<Animal> animals = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
            String dLine = reader.readLine();
            String grassLine = reader.readLine();
            String nLine = reader.readLine();

            if (dLine == null || grassLine == null || nLine == null) {
                throw new InvalidInputsException();
            }

            int days = validateDays(dLine.trim());
            float grassAmount = validateGrassAmount(grassLine.trim());
            int numberOfAnimals = validateNumberOfAnimals(nLine.trim());

            for (int i = 0; i < numberOfAnimals; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new InvalidNumberOfAnimalParametersException();
                }
                animals.add(validateAndCreateAnimal(line.trim()));
            }

            reader.close();

            runSimulation(days, grassAmount, animals);
            printAnimals(animals);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
