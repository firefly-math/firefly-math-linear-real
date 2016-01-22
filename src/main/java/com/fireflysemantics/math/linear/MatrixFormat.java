/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.fireflysemantics.math.linear;

import static com.fireflysemantics.math.exception.ExceptionKeys.VALUE;
import static com.fireflysemantics.math.exception.ExceptionTypes.MATRIX_PARSE;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fireflysemantics.math.exception.ExceptionKeys;
import com.fireflysemantics.math.exception.MathException;
import com.fireflysemantics.math.format.CompositeFormat;
import com.fireflysemantics.math.linear.matrix.ArrayMatrix;

import lombok.Getter;

/**
 * Formats a {@code nxm} matrix in components list format "{{a<sub>0</sub>
 * <sub>0</sub>,a<sub>0</sub><sub>1</sub>, ..., a<sub>0</sub><sub>m-1</sub>},{a
 * <sub>1</sub><sub>0</sub>, a<sub>1</sub><sub>1</sub>, ..., a<sub>1</sub>
 * <sub>m-1</sub>},{...},{ a<sub>n-1</sub><sub>0</sub>, a<sub>n-1</sub>
 * <sub>1</sub>, ..., a<sub>n-1</sub><sub>m-1</sub>}}".
 * <p>
 * The prefix and suffix "{" and "}", the row prefix and suffix "{" and "}", the
 * row separator "," and the column separator "," can be replaced by any
 * user-defined strings. The number format for components can be configured.
 * </p>
 *
 * <p>
 * White space is ignored at parse time, even if it is in the prefix, suffix or
 * separator specifications. So even if the default separator does include a
 * space character that is used at format time, both input string "{{1,1,1}}"
 * and " { { 1 , 1 , 1 } } " will be parsed without error and the same matrix
 * will be returned. In the second case, however, the parse position after
 * parsing will be just after the closing curly brace, i.e. just before the
 * trailing space.
 * </p>
 *
 * <p>
 * <b>Note:</b> the grouping functionality of the used {@link NumberFormat} is
 * disabled to prevent problems when parsing (e.g. 1,345.34 would be a valid
 * number but conflicts with the default column separator).
 * </p>
 *
 */
public class MatrixFormat {

	/** The default prefix: "{". */
	private static final String DEFAULT_PREFIX = "{";
	/** The default suffix: "}". */
	private static final String DEFAULT_SUFFIX = "}";
	/** The default row prefix: "{". */
	private static final String DEFAULT_ROW_PREFIX = "{";
	/** The default row suffix: "}". */
	private static final String DEFAULT_ROW_SUFFIX = "}";
	/** The default row separator: ",". */
	private static final String DEFAULT_ROW_SEPARATOR = ",";
	/** The default column separator: ",". */
	private static final String DEFAULT_COLUMN_SEPARATOR = ",";
	/**
	 * Prefix.
	 * 
	 * @return format prefix.
	 */
	@Getter
	private final String prefix;
	/**
	 * Suffix.
	 * 
	 * @return format suffix.
	 */
	@Getter
	private final String suffix;
	/**
	 * Row prefix.
	 * 
	 * @return format row prefix.
	 */
	@Getter
	private final String rowPrefix;
	/**
	 * Row suffix.
	 * 
	 * @return format row suffix.
	 */
	@Getter
	private final String rowSuffix;

	/**
	 * Row separator.
	 * 
	 * @return format separator for rows.
	 */
	@Getter
	private final String rowSeparator;
	/**
	 * Column separator.
	 * 
	 * @return format separator between components.
	 */
	@Getter
	private final String columnSeparator;
	/**
	 * The format used for components.
	 * 
	 * @return components format.
	 */
	@Getter
	private final NumberFormat format;

	/**
	 * Create an instance with default settings.
	 * <p>
	 * The instance uses the default prefix, suffix and row/column separator:
	 * "[", "]", ";" and ", " and the default number format for components.
	 * </p>
	 */
	public MatrixFormat() {
		this(DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_ROW_PREFIX, DEFAULT_ROW_SUFFIX, DEFAULT_ROW_SEPARATOR,
				DEFAULT_COLUMN_SEPARATOR, CompositeFormat.getDefaultNumberFormat());
	}

	/**
	 * Create an instance with a custom number format for components.
	 * 
	 * @param format
	 *            the custom format for components.
	 */
	public MatrixFormat(final NumberFormat format) {
		this(DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_ROW_PREFIX, DEFAULT_ROW_SUFFIX, DEFAULT_ROW_SEPARATOR,
				DEFAULT_COLUMN_SEPARATOR, format);
	}

	/**
	 * Create an instance with custom prefix, suffix and separator.
	 * 
	 * @param prefix
	 *            prefix to use instead of the default "{"
	 * @param suffix
	 *            suffix to use instead of the default "}"
	 * @param rowPrefix
	 *            row prefix to use instead of the default "{"
	 * @param rowSuffix
	 *            row suffix to use instead of the default "}"
	 * @param rowSeparator
	 *            tow separator to use instead of the default ";"
	 * @param columnSeparator
	 *            column separator to use instead of the default ", "
	 */
	public MatrixFormat(final String prefix, final String suffix, final String rowPrefix,
			final String rowSuffix, final String rowSeparator, final String columnSeparator) {
		this(prefix, suffix, rowPrefix, rowSuffix, rowSeparator, columnSeparator,
				CompositeFormat.getDefaultNumberFormat());
	}

	/**
	 * Create an instance with custom prefix, suffix, separator and format for
	 * components.
	 * 
	 * @param prefix
	 *            prefix to use instead of the default "{"
	 * @param suffix
	 *            suffix to use instead of the default "}"
	 * @param rowPrefix
	 *            row prefix to use instead of the default "{"
	 * @param rowSuffix
	 *            row suffix to use instead of the default "}"
	 * @param rowSeparator
	 *            tow separator to use instead of the default ";"
	 * @param columnSeparator
	 *            column separator to use instead of the default ", "
	 * @param format
	 *            the custom format for components.
	 */
	public MatrixFormat(final String prefix, final String suffix, final String rowPrefix,
			final String rowSuffix, final String rowSeparator, final String columnSeparator,
			final NumberFormat format) {
		this.prefix = prefix;
		this.suffix = suffix;
		this.rowPrefix = rowPrefix;
		this.rowSuffix = rowSuffix;
		this.rowSeparator = rowSeparator;
		this.columnSeparator = columnSeparator;
		this.format = format;
		// disable grouping to prevent parsing problems
		this.format.setGroupingUsed(false);
	}

	/**
	 * Get the set of locales for which real vectors formats are available.
	 * <p>
	 * This is the same set as the {@link NumberFormat} set.
	 * </p>
	 * 
	 * @return available real vector format locales.
	 */
	public static Locale[] getAvailableLocales() {
		return NumberFormat.getAvailableLocales();
	}

	/**
	 * Returns the default real vector format for the current locale.
	 * 
	 * @return the default real vector format.
	 */
	public static MatrixFormat getInstance() {
		return getInstance(Locale.getDefault());
	}

	/**
	 * Returns the default real vector format for the given locale.
	 * 
	 * @param locale
	 *            the specific locale used by the format.
	 * @return the real vector format specific to the given locale.
	 */
	public static MatrixFormat getInstance(final Locale locale) {
		return new MatrixFormat(CompositeFormat.getDefaultNumberFormat(locale));
	}

	/**
	 * This method calls {@link #format(RealMatrix,StringBuffer,FieldPosition)}.
	 *
	 * @param m
	 *            RealMatrix object to format.
	 * @return a formatted matrix.
	 */
	public String format(ArrayMatrix m) {
		return format(m, new StringBuffer(), new FieldPosition(0)).toString();
	}

	/**
	 * Formats a {@link RealMatrix} object to produce a string.
	 * 
	 * @param matrix
	 *            the object to format.
	 * @param toAppendTo
	 *            where the text is to be appended
	 * @param pos
	 *            On input: an alignment field, if desired. On output: the
	 *            offsets of the alignment field
	 * @return the value passed in as toAppendTo.
	 */
	public StringBuffer format(ArrayMatrix matrix, StringBuffer toAppendTo, FieldPosition pos) {

		pos.setBeginIndex(0);
		pos.setEndIndex(0);

		// format prefix
		toAppendTo.append(prefix);

		// format rows
		final int rows = matrix.getRowDimension();
		for (int i = 0; i < rows; ++i) {
			toAppendTo.append(rowPrefix);
			for (int j = 0; j < matrix.getColumnDimension(); ++j) {
				if (j > 0) {
					toAppendTo.append(columnSeparator);
				}
				CompositeFormat.formatDouble(matrix.getEntry(i, j), format, toAppendTo, pos);
			}
			toAppendTo.append(rowSuffix);
			if (i < rows
					- 1) {
				toAppendTo.append(rowSeparator);
			}
		}

		// format suffix
		toAppendTo.append(suffix);

		return toAppendTo;
	}

	/**
	 * Parse a string to produce a {@code double[][] } array.
	 *
	 * @param source
	 *            String to parse.
	 * @return the parsed {@code double[][] } instance.
	 * @throws MathException
	 *             Of type {@code PARSE} if the beginning of the specified
	 *             string cannot be parsed.
	 */
	public double[][] parse(String source) {
		final ParsePosition parsePosition = new ParsePosition(0);
		final double[][] result = parse(source, parsePosition);
		if (parsePosition.getIndex() == 0) {
			throw new MathException(MATRIX_PARSE).put(VALUE, source).put(ExceptionKeys.INDEX,
					parsePosition.getErrorIndex());
		}
		return result;
	}

	/**
	 * Parse a string to produce a {@double[][] matrix} representation.
	 *
	 * @param source
	 *            String to parse.
	 * @param pos
	 *            input/ouput parsing parameter.
	 * @return the parsed {@code double[][] } object.
	 */
	public double[][] parse(String source, ParsePosition pos) {
		int initialIndex = pos.getIndex();

		final String trimmedPrefix = prefix.trim();
		final String trimmedSuffix = suffix.trim();
		final String trimmedRowPrefix = rowPrefix.trim();
		final String trimmedRowSuffix = rowSuffix.trim();
		final String trimmedColumnSeparator = columnSeparator.trim();
		final String trimmedRowSeparator = rowSeparator.trim();

		// parse prefix
		CompositeFormat.parseAndIgnoreWhitespace(source, pos);
		if (!CompositeFormat.parseFixedstring(source, trimmedPrefix, pos)) {
			return null;
		}

		// parse components
		List<List<Number>> matrix = new ArrayList<List<Number>>();
		List<Number> rowComponents = new ArrayList<Number>();
		for (boolean loop = true; loop;) {

			if (!rowComponents.isEmpty()) {
				CompositeFormat.parseAndIgnoreWhitespace(source, pos);
				if (!CompositeFormat.parseFixedstring(source, trimmedColumnSeparator, pos)) {
					if (trimmedRowSuffix.length() != 0
							&& !CompositeFormat.parseFixedstring(source, trimmedRowSuffix, pos)) {
						return null;
					} else {
						CompositeFormat.parseAndIgnoreWhitespace(source, pos);
						if (CompositeFormat.parseFixedstring(source, trimmedRowSeparator, pos)) {
							matrix.add(rowComponents);
							rowComponents = new ArrayList<Number>();
							continue;
						} else {
							loop = false;
						}
					}
				}
			} else {
				CompositeFormat.parseAndIgnoreWhitespace(source, pos);
				if (trimmedRowPrefix.length() != 0
						&& !CompositeFormat.parseFixedstring(source, trimmedRowPrefix, pos)) {
					return null;
				}
			}

			if (loop) {
				CompositeFormat.parseAndIgnoreWhitespace(source, pos);
				Number component = CompositeFormat.parseNumber(source, format, pos);
				if (component != null) {
					rowComponents.add(component);
				} else {
					if (rowComponents.isEmpty()) {
						loop = false;
					} else {
						// invalid component
						// set index back to initial, error index should already
						// be set
						pos.setIndex(initialIndex);
						return null;
					}
				}
			}

		}

		if (!rowComponents.isEmpty()) {
			matrix.add(rowComponents);
		}

		// parse suffix
		CompositeFormat.parseAndIgnoreWhitespace(source, pos);
		if (!CompositeFormat.parseFixedstring(source, trimmedSuffix, pos)) {
			return null;
		}

		// do not allow an empty matrix
		if (matrix.isEmpty()) {
			pos.setIndex(initialIndex);
			return null;
		}

		// build vector
		double[][] data = new double[matrix.size()][];
		int row = 0;
		for (List<Number> rowList : matrix) {
			data[row] = new double[rowList.size()];
			for (int i = 0; i < rowList.size(); i++) {
				data[row][i] = rowList.get(i).doubleValue();
			}
			row++;
		}
		return data;
	}
}