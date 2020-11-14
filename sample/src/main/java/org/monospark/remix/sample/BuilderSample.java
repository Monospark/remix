package org.monospark.remix.sample;

import org.monospark.remix.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuilderSample {

    public static void main(String[] args) throws IOException {
        Car c1 = Records.builder(Car.class)
                .set(Car::manufacturer, () -> "RemixCars")
                .set(Car::model, () -> "The Budget car")
                .set(Car::price, () -> 10000)
                .set(Car::available, () -> true)
                .build();

        RecordBlank<Car> carBlank = Records.builder(Car.class)
                .set(Car::manufacturer, () -> "RemixCars")
                .blank();

        Car c2 = Records.builder(carBlank)
                .set(Car::model, () -> "The luxurious car")
                .set(Car::price, () -> 60000)
                .set(Car::available, () -> true)
                .build();

//        var cs = Records.blank(CarStatus.class)
//                .set(CarStatus::speed,() -> 50)
//                //.set(CarStatus::gear, 2)
//                .set(CarStatus::b1, () -> false)
//                .set(CarStatus::b2, () -> true)
//                .set(CarStatus::b3, () -> false)
//                .build();

        System.out.println(c1);
        System.out.println(c2);

        List<Car> cars = new ArrayList<>();
        cars.add(c1);
        cars.add(c2);
        CarDatabase d = Records.create(CarDatabase.class, cars);

        // Doesn't alter the database
        cars.clear();

        List<Car> databaseContent = Records.get(d::cars);
        // Throws an exception, since the returned view is unmodifiable
        databaseContent.clear();

        var out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(c1);
            oos.flush();
            byte[] b = out.toByteArray();

        System.out.println(new String(b));

            ByteArrayInputStream in = new ByteArrayInputStream(b);
            ObjectInputStream ois = new ObjectInputStream(in);
        try {
            Car da = (Car) ois.readObject();
            System.out.println(Records.get(da::available));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public static class CarStatusBlank extends RecordRemix<CarStatus> {
        @Override
        public void blank(RecordBuilder<CarStatus> builder) {
            builder.set(CarStatus::speed, () -> 1);
        }

        @Override
        public void get(RecordOperations<CarStatus> ops) {

        }

        @Override
        public void assign(RecordOperations<CarStatus> ops) {

        }

        @Override
        public void set(RecordOperations<CarStatus> ops) {

        }
    }

    public static class CarRemix extends RecordRemix<Car> {
        @Override
        public void blank(RecordBuilder<Car> builder) {
            builder.set(Car::manufacturer, () -> "RemixCars");
        }

        @Override
        public void assign(RecordOperations<Car> operations) {
            operations.notNull(Car::manufacturer)
                    .notNull(Car::model)
                    .check(Car::price, p -> p > 0);
        }
    }

    @Remix(CarDatabaseRemix.class)
    public record CarDatabase(Wrapped<List<Car>> cars) implements Serializable {


    }

    public record CarDatabaseSer(List<Car> cars) implements Serializable {
    }


    @Remix(CarRemix.class)
    public static record Car(Wrapped<String> manufacturer, Wrapped<String> model, WrappedInt price,
                             MutableBoolean available) implements Serializable{
    }


    public record CarW(String manufacturer, String model, int price, boolean available) {
    }

    @Remix(CarStatusBlank.class)
    public record CarStatus(int speed, int gear, boolean lightsOn, boolean wipersOn) {

    }

    public static class CarDatabaseRemix extends RecordRemix<CarDatabase> {
        @Override
        public void blank(RecordBuilder<CarDatabase> builder) {
            // The default value for the car list should be an empty array list
            builder.set(CarDatabase::cars, () -> new ArrayList<>());
        }

        @Override
        public void get(RecordOperations<CarDatabase> ops) {
            // Return an unmodifiable list view to prevent tampering with the database from outside this instance
            ops.add(CarDatabase::cars, Collections::unmodifiableList);
        }

        @Override
        public void assign(RecordOperations<CarDatabase> ops) {
            // Check for null and make a defensive copy of the list when constructing an instance.
            ops.notNull(CarDatabase::cars)
                    .check(CarDatabase::cars, c -> !c.contains(null))
                    .add(CarDatabase::cars, ArrayList::new);
        }
    }
}
