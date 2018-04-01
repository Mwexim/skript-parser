/**
 * This package is used for converting plaintext files to a tree object structure that can later be interpreted as
 * runnable code by this parser. In no way do either of {@link io.github.syst3ms.skriptparser.file.FileElement} or
 * {@link io.github.syst3ms.skriptparser.file.FileSection} have any information on what the text means to the parser ;
 * they are just object representations of the text in a file.
 * The logic of actual runnable code and its object representation is defined inside of the
 * {@linkplain io.github.syst3ms.skriptparser.lang lang package}.
 */
@ParametersAreNonnullByDefault
package io.github.syst3ms.skriptparser.file;

import javax.annotation.ParametersAreNonnullByDefault;