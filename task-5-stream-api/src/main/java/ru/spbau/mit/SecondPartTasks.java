package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths.stream()
            .map(Paths::get)
            .flatMap(SecondPartTasks::getLines)
            .filter(s -> s.contains(sequence))
            .collect(Collectors.toList());
    }

    private static Stream<String> getLines(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        class Shot {
            private final double radius = 0.5;
            private final double x = ThreadLocalRandom.current().nextDouble(1.0);
            private final double y = ThreadLocalRandom.current().nextDouble(1.0);

            private boolean isHit() {
                double fromCenterX = x - radius;
                double fromCenterY = y - radius;
                double distanceFromCenter = Math.sqrt(Math.pow(fromCenterX, 2) + Math.pow(fromCenterY, 2));

                return distanceFromCenter <= radius;
            }
        }

        IntSummaryStatistics stats = Stream.generate(Shot::new).limit(1000000).collect(
            Collectors.summarizingInt(s -> s.isHit() ? 1 : 0)
        );

        return (double) stats.getSum() / stats.getCount();
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static Optional<String> findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet().stream()
            .max(Comparator.comparing(e -> String.join("", e.getValue()).length()))
            .map(Map.Entry::getKey);
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ
    // в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream()
            .flatMap(m -> m.entrySet().stream())
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.summingInt(Map.Entry::getValue)
            ));
    }
}
