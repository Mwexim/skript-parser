package io.github.syst3ms.skriptparser.file;

/**
 * Represents any line in a file that isn't blank or a comment only
 */
public interface FileElement {
    String getLineContent();
}
