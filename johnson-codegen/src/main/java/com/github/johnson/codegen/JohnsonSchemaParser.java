package com.github.johnson.codegen;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import com.github.johnson.codegen.types.JohnsonType;

public interface JohnsonSchemaParser extends Closeable {
	Map<String, JohnsonType> read() throws IOException;
}
