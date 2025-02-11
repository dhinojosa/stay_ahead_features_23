package com.evolutionnext.streamgatherers;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.*;

@SuppressWarnings("preview")
public class SimpleGathererTest {

    private List<List<Integer>> expected = List.of(
        List.of(1, 2),
        List.of(3),
        List.of(4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
        List.of(14, 15),
        List.of(16, 17, 18, 19, 20, 21, 22, 23, 24, 25),
        List.of(26, 27),
        List.of(28, 29, 30, 31, 32, 33, 34, 35, 36, 37),
        List.of(38, 39, 40, 41, 42, 43, 44, 45, 46, 47),
        List.of(48, 49)
    );

    boolean isPrime(int i) {
        if (i <= 1) return false;
        if (i == 2) return true;
        if (i % 2 == 0) return false;
        for (int j = 3; j * j <= i; j += 2) {
            if (i % j == 0) return false;
        }
        return true;
    }

    @Test
    void testSimpleGatherer() {
        var gatherer = new Gatherer<Integer, ArrayList<Integer>, List<Integer>>() {

            @Override
            public Supplier<ArrayList<Integer>> initializer() {
                return ArrayList::new;
            }

            @Override
            public Integrator<ArrayList<Integer>, Integer, List<Integer>> integrator() {
                return (state, element, downstream) -> {
                    state.add(element);
                    if (isPrime(state.stream().mapToInt(x -> x).sum()) || state.size() == 10) {
                        ArrayList<Integer> downstreamList = new ArrayList<>(state);
                        downstream.push(downstreamList);
                        state.removeAll(downstreamList);
                    }
                    return true;
                };
            }

            @Override
            public BiConsumer<ArrayList<Integer>, Downstream<? super List<Integer>>> finisher() {
               return (state, downstream) -> {
                   if (isPrime(state.stream().mapToInt(x -> x).sum()) || state.size() == 10) {
                       downstream.push(state);
                   }
               };
            }
        };
        List<List<Integer>> actual = IntStream.rangeClosed(1, 50).boxed().gather(gatherer).collect(Collectors.toList());
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testGathererOfStaticMethod() {
        Gatherer.Integrator<ArrayList<Integer>, Integer, List<Integer>> integrator = (state, element, downstream) -> {
            state.add(element);
            if (isPrime(state.stream().mapToInt(x -> x).sum()) || state.size() == 10) {
                ArrayList<Integer> downstreamList = new ArrayList<>(state);
                downstream.push(downstreamList);
                state.removeAll(downstreamList);
            }
            return true;
        };
        Gatherer<Integer, ArrayList<Integer>, List<Integer>> gatherer =
            Gatherer.of(ArrayList::new, integrator, (integers, integers2) -> {
                integers.addAll(integers2);
                return integers;
            }, (state, downstream) -> {
                if (isPrime(state.stream().mapToInt(x -> x).sum()) || state.size() == 10) {
                    downstream.push(state);
                }
            });

        List<List<Integer>> actual = IntStream.rangeClosed(1, 50).boxed().gather(gatherer).collect(Collectors.toList());
        System.out.println(actual);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testGathererNonGreedy() {
        Gatherer.Integrator<ArrayList<Integer>, Integer, List<Integer>> nonGreedyIntegrator = Gatherer.Integrator.of((state, element, downstream) -> {
            state.add(element);
            if (state.size() == 10) {
                ArrayList<Integer> downstreamList = new ArrayList<>(state);
                downstream.push(downstreamList);
                state.removeAll(downstreamList);
            }
            return true;
        });

        Gatherer<Integer, ArrayList<Integer>, List<Integer>> nonGreedyGatherer = Gatherer.of(ArrayList::new, nonGreedyIntegrator, (integers, integers2) -> {
            integers.addAll(integers2);
            return integers;
        }, (integers, downstream) -> {
            if (integers.size() == 10) {
                downstream.push(integers);
            }
        });

        System.out.println(Stream.iterate(1, i -> i + 1).gather(nonGreedyGatherer).limit(100).toList());

    }
    @Test
    void testGathererGreedy() {
        Gatherer.Integrator.Greedy<ArrayList<Integer>, Integer, List<Integer>> greedyIntegrator = Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
            state.add(element);
            if (state.size() == 10) {
                ArrayList<Integer> downstreamList = new ArrayList<>(state);
                downstream.push(downstreamList);
                state.removeAll(downstreamList);
            }
            return true;
        });



        Gatherer<Integer, ArrayList<Integer>, List<Integer>> greedyGatherer = Gatherer.of(ArrayList::new, greedyIntegrator, (integers, integers2) -> {
            integers.addAll(integers2);
            return integers;
        }, (integers, downstream) -> {
            if (integers.size() == 10) {
                downstream.push(integers);
            }
        });



        System.out.println(Stream.iterate(1, i -> i + 1).gather(greedyGatherer).limit(10).toList());

    }
}
