/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package io.github.syst3ms.skriptparser.types.conversions;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Used to chain convertes to build a single converter. This is automatically created when a new converter is added.
 *
 * @author Peter Güttinger
 * @param <F> same as Converter's <F> (from)
 * @param <M> the middle type, i.e. the type the first converter converts to and the second converter comverts from.
 * @param <T> same as Converter's <T> (to)
 * @see Converters#registerConverter(Class, Class, Function)
 */
public final class ChainedConverter<F, M, T> implements Function<F, T> {
    private final Function<? super F, ? extends M> first;
    private final Function<? super M, ? extends T> second;

    public ChainedConverter(Function<? super F, ? extends M> first, Function<? super M, ? extends T> second) {
        this.first = first;
        this.second = second;
    }

    @SuppressWarnings("unchecked")
    public static <F, M, T> ChainedConverter<F, M, T> newInstance(Function<? super F, ?> first, Function<?, ? extends T> second) {
        return new ChainedConverter<>((Function<? super F, ? extends M>) first, (Function<? super M, ? extends T>) second);
    }

    @Override
    @Nullable
    public T apply(@Nullable F f) {
        M m = first.apply(f);
        if (m == null)
            return null;
        return second.apply(m);
    }

}