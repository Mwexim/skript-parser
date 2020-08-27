package io.github.syst3ms.skriptparser.file;

/**
 * A {@link FileElement} representing a blank line.
 */
public class VoidElement extends FileElement {
    public VoidElement(String fileName, int line, int indentation) {
        super(fileName, line, "", indentation);
    }
}
