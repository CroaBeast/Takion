package me.croabeast.file;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@UtilityClass
class MapUtils {

    @Getter
    static class MappableImpl<T, C extends Collection<T>, M extends Mappable<T, C, M>>
            extends LinkedHashMap<Integer, C> implements Mappable<T, C, M> {

        protected final Supplier<C> supplier;

        MappableImpl(Supplier<C> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        static class Set<T> extends MappableImpl<T, java.util.Set<T>, Mappable.Set<T>> implements Mappable.Set<T> {

            Set() {
                super(HashSet::new);
            }
        }

        static class List<T> extends MappableImpl<T, java.util.List<T>, Mappable.List<T>> implements Mappable.List<T> {

            List() {
                super(ArrayList::new);
            }
        }
    }

    static class SectionImpl<C extends Collection<ConfigurationSection>>
            extends MappableImpl<ConfigurationSection, C, SectionMappable<C>>
            implements SectionMappable<C> {

        SectionImpl(Supplier<C> supplier) {
            super(supplier);
        }

        static class Set extends SectionImpl<java.util.Set<ConfigurationSection>> implements SectionMappable.Set {

            Set() {
                super(HashSet::new);
            }

            @NotNull
            public SectionMappable.Set instance() {
                return this;
            }

            @NotNull
            public SectionMappable.Set copy() {
                SectionImpl.Set map = new SectionImpl.Set();
                map.putAll(this);
                return map;
            }
        }

        static class List extends SectionImpl<java.util.List<ConfigurationSection>> implements SectionMappable.List {

            List() {
                super(ArrayList::new);
            }

            @NotNull
            public SectionMappable.List instance() {
                return this;
            }

            @NotNull
            public SectionMappable.List copy() {
                SectionImpl.List map = new SectionImpl.List();
                map.putAll(this);
                return map;
            }
        }
    }

    static class UnitImpl<U extends ConfigurableUnit, C extends Collection<U>>
            extends MappableImpl<U, C, UnitMappable<U, C>> implements UnitMappable<U, C> {

        UnitImpl(Supplier<C> supplier) {
            super(supplier);
        }

        @NotNull
        public UnitMappable<U, C> instance() {
            return this;
        }

        @NotNull
        public UnitMappable<U, C> copy() {
            UnitImpl<U, C> map = new UnitImpl<>(supplier);
            map.putAll(this);
            return map;
        }

        static class Set<U extends ConfigurableUnit> extends UnitImpl<U, java.util.Set<U>> implements UnitMappable.Set<U> {

            Set() {
                super(HashSet::new);
            }

            @NotNull
            public UnitMappable.Set<U> instance() {
                return this;
            }

            @NotNull
            public UnitMappable.Set<U> copy() {
                UnitImpl.Set<U> map = new UnitImpl.Set<>();
                map.putAll(this);
                return map;
            }
        }

        static class List<U extends ConfigurableUnit> extends UnitImpl<U, java.util.List<U>> implements UnitMappable.List<U> {

            List() {
                super(ArrayList::new);
            }

            @NotNull
            public UnitMappable.List<U> instance() {
                return this;
            }

            @NotNull
            public UnitMappable.List<U> copy() {
                UnitImpl.List<U> map = new UnitImpl.List<>();
                map.putAll(this);
                return map;
            }
        }
    }
}
