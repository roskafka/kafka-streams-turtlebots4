package de.hfu;

public class X {

    public static class Y<K, V> {
        private final K key;
        private final V value;

        public Y(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        public <K, V> Y<K, V> build() {
            return new Y<>(null, null);
        }
    }

    public static void main(String[] args) {
        Y<String, String> y = builder().build();
        System.out.println(y.getKey());
        System.out.println(y.getValue());
    }
}
