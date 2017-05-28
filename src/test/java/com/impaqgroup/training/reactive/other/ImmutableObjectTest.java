package com.impaqgroup.training.reactive.other;

import static java.util.Collections.unmodifiableList;

import java.util.List;

public class ImmutableObjectTest {

    static public final class Immutable{

        private final String name;

        public Immutable(String n){
            this.name = n;
        }

        public String getName(){
            return name;
        }
    }

    static public final class NoMod {

        private final List<String> l;

        public NoMod(List<String> l) {
            this.l = l;
        }

        public List<String> getList() {
            return unmodifiableList(l);
        }
    }
}
