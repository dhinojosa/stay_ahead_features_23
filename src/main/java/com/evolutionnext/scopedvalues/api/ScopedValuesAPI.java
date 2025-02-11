package com.evolutionnext.scopedvalues.api;

import java.util.concurrent.StructuredTaskScope;

public class ScopedValuesAPI {
    static ScopedValue<String> GREETING_KEY = ScopedValue.newInstance();
    static ScopedValue<String> FAREWELL_KEY = ScopedValue.newInstance();

    /**
     * The main method that calls various API calls
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        whereAndCall();
        whereAndRun();
        getWhere();
        callWhere();
        runWhere();
        doubleWhereAndCall();
        isBound();
        orElse();
    }


    /**
     * `where` returns a `Carrier`, which holds the mappings
     * for ScopedValue per thread.
     * `call` takes a `Callable`
     */
    private static void whereAndCall() throws Exception {
        String outerResult = ScopedValue.where(GREETING_KEY, "Bon Jour").call(() -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var result = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                scope.join();
                return result.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }

    /**
     * `where` returns a `Carrier`, which holds the mappings
     * for ScopedValue per thread.
     * `run` takes a `Runnable`
     */
    private static void whereAndRun() {
        ScopedValue.where(GREETING_KEY, "Здравейте").run(printGreetingKey());
    }

    /**
     * Synonymous with where().get()
     */
    private static void getWhere() {
        String outerResult = ScopedValue.callWhere(GREETING_KEY, "Hola", () -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var result = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                scope.join();
                return result.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }

    /**
     * Synonymous with `where().call()`
     *
     * @throws Exception if an exception occurs during the execution of the code block
     */
    private static void callWhere() throws Exception {
        String outerResult = ScopedValue.callWhere(GREETING_KEY, "こんにちは", () -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var result = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                scope.join();
                return result.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }

    /**
     * Synonymous with `where().run()`
     */
    private static void runWhere() {
        ScopedValue.runWhere(GREETING_KEY, "नमस्ते", printGreetingKey());
    }

    private static Runnable printGreetingKey() {
        return () -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var result = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                scope.join();
                System.out.println(result.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static void doubleWhereAndCall() throws Exception {
        String outerResult = ScopedValue.where(GREETING_KEY, "Γειά σου").where(FAREWELL_KEY, "Antio sas").call(() -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var helloSubtask = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                var goodbyeSubtask = scope.fork(() -> String.format("%s!", FAREWELL_KEY.get()));
                scope.join();
                return String.format("%s %s", helloSubtask.get(), goodbyeSubtask.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }

    /**
     * You can use the `ScopedValue#isBound` to determine if the key is bound
     * at a position in your code
     *
     * @throws Exception when the call cannot complete
     */
    private static void isBound() throws Exception {
        String outerResult = ScopedValue.where(GREETING_KEY, "你好").call(() -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var helloSubtask = scope.fork(() -> String.format("%s!", GREETING_KEY.get()));
                scope.join();
                GREETING_KEY.isBound();
                return String.format("%s", helloSubtask.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }

    /**
     * You can use the `ScopedValue#isBound` to determine if the key is bound
     * at a position in your code
     *
     * @throws Exception when the call cannot complete
     */
    private static void orElse() throws Exception {
        String outerResult = ScopedValue.where(GREETING_KEY, "Selamat datang").call(() -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var helloSubtask = scope.fork(() -> String.format("%s!", GREETING_KEY.orElse("Olá")));
                scope.join();
                return String.format("%s", helloSubtask.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(outerResult);
    }
}
