package org.ryan.sentry;

import java.util.function.Supplier;
import io.sentry.Sentry;
import io.sentry.context.Context;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;

public class Main implements Thread.UncaughtExceptionHandler {

    public static void main(String... args) {
        Thread.setDefaultUncaughtExceptionHandler(new Main());
        System.err.println("About to init...");
        Sentry.init();
        System.err.println("Finished init, about to log to sentry...");
        Sentry.init();
        for (int i = 0; i < 10; i += 1) {
            final int index = i;
            new Thread(() -> doThings("user1", index)).start();
            new Thread(() -> doThings("user2", index)).start();
            new Thread(() -> doThings("user3", index)).start();
        }
        // logWithSentry();
        System.err.println("All done");
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String userId = user.get();
        System.err.println("[" + userId + "] Exception caught! " + e.getMessage());
        sentry(t, e, userId);
    }

    private static void sentry(Thread t, Throwable e, String userId) {
        Sentry.getContext().setUser(new UserBuilder().setEmail(userId).build());
        Sentry.capture(e);
    }

    private static ThreadLocal<String> user = new ThreadLocal<>();
    private static void doThings(String userToUse, int i) {
        new Thread(() -> {
            user.set(userToUse);
            unsafeMethod1();
        }).start();
        new Thread(() -> {
            user.set(userToUse);
            unsafeMethod2();
        }).start();
        new Thread(() -> {
            user.set(userToUse);
            unsafeMethod3(10, i);
        }).start();
    }

    private static void unsafeMethod1() {
        throw new UnsupportedOperationException("You shouldn't call this!");
    }

    private static void unsafeMethod2() {
        System.err.println(getSomething().get());
    }

    private static void unsafeMethod3(int x, int y) {
        if (x / y > 2) {
            System.err.println("greater than");
        } else {
            System.err.println("less than");
        }
    }

    private static int somethingCount = 0;

    private static Supplier<String> getSomething() {
        if (somethingCount++ % 3 == 0) {
            return null;
        }
        return () -> "something";
    }

    private static void logWithSentry() {
        Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage("User made an action").build());
        Sentry.getContext().setUser(new UserBuilder().setEmail("hello@sentry.io").build());
        Sentry.getContext().addExtra("extra", "thing");
        Sentry.getContext().addTag("tagName", "tagValue");
        Sentry.capture("This is a test");
        try {
            unsafeMethod1();
        } catch (Exception e) {
            Sentry.capture(e);
        }
    }

}
