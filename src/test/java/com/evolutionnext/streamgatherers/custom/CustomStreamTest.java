package com.evolutionnext.streamgatherers.custom;


import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class CustomStreamTest {

//    @Test
//    void testCustomStreamsWithZeroEach() {
//        Stream<Integer> a = Stream.empty();
//        Stream<Character> b = Stream.empty();
//
//
//        System.out.println(a.gather(gatherer).toList());
//    }

    @Test
    void testCustomStreamsWithOneInEachEach() {
        Stream<Integer> a = Stream.of(1,2);
        Stream<Character> b = Stream.of('a', 'b');

        record MyPair<A, B>(A first, B second) {}

        var biFunction = new BiFunction<Integer, Character, String>() {
            @Override
            public String apply(Integer x, Character y) {
                return x + "-" + y;
            }
        };

        Gatherer<MyPair<Integer, Character>, AtomicReference<Integer>, String> gatherer = new Gatherer<MyPair<Integer, Character>, AtomicReference<Integer>, String>() {

            @Override
            public Supplier<AtomicReference<Integer>> initializer() {
                return AtomicReference::new;
            }

            @Override
            public Integrator<AtomicReference<Integer>, MyPair<Integer, Character>, String> integrator() {
                return new Integrator<AtomicReference<Integer>, MyPair<Integer, Character>, String>() {
                    @Override
                    public boolean integrate(AtomicReference<Integer> atomicReference, MyPair<Integer, Character> element, Downstream<? super String> downstream) {
                        Integer i = atomicReference.get();
                        if (!element.first().equals(i)) {
                            atomicReference.set(i);
                            downstream.push(biFunction.apply(element.first(), element.second()));
                        }
                        return true;
                    }
                };
            }


        };

        Stream.of(1,2,3).flatMap(x -> Stream.of('a', 'b').map(y -> new MyPair<>(x, y))).gather(gatherer).forEach(System.out::println);
    }
}
