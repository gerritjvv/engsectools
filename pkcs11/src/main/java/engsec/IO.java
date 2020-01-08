package engsec;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

public class IO {

    public static final void prn(Object... args) {
        prn(Arrays.asList(args));
    }

    public static final void prn(Enumeration args) {
       prn(Collections.list(args));
    }

    public static final void prn(Collection args) {
        if (args == null) {
            System.out.println("null");

        } else {
            System.out.println(
                    args
                            .stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(" ")));
        }
    }
}
