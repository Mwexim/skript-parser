package io.github.syst3ms.skriptparser.file;

/**
 * Represents any non-blank and not comment-only line in a file. It is important to note that information about comments
 * is absent from this class, as they are discarded before being passed to the constructor.<br>
 * <br>
 * <strong>IMPORTANT : even though {@link FileSection} inherits from this class, they are semantically different ; that is,
 * {@link FileSection} will be treated separately, rather than just being treated as some inheritor with
 * additional properties.</strong>
 */
public class FileElement {
    private final String fileName;
    private final int line;
    private final String content;
    private final int indentation;

    public FileElement(String fileName, int line, String content, int indentation) {
        this.fileName = fileName;
        this.line = line;
        this.content = content;
        this.indentation = indentation;
    }

    /**
     * The returned {@link String} does not include the indentation of the line. To have the line content along with
     * indentation, use {@link #toString()}
     * @return the text content of this line, excluding any indentation.
     */
    public String getLineContent() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FileElement)) {
            return false;
        } else {
            var other = (FileElement) obj;
            return indentation == other.indentation &&
                   line == other.line &&
                   content.equalsIgnoreCase(other.content) &&
                   fileName.equals(other.fileName);
        }
    }

    /**
     * @return how much this line is indented, e.g shifted by either a tab character or 4 spaces.
     */
    public int getIndentation() {
        return indentation;
    }

    @Override
    public String toString() {
        return "    ".repeat(indentation) + content;
    }

    /**
     * @return the name of the file this line is contained in
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Line numbering starts at 1, and blank and comment-only lines are accounted for.
     * @return the line in the file where this line is located at.
     */
    public int getLine() {
        return line;
    }
}
