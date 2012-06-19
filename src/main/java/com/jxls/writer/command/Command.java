package com.jxls.writer.command;

import com.jxls.writer.area.Area;
import com.jxls.writer.common.CellRef;
import com.jxls.writer.common.Context;
import com.jxls.writer.common.Size;

import java.util.List;

/**
 * A command defines a transformation of a list of areas at a specified cell
 *
 * Date: Mar 13, 2009
 * @author Leonid Vysochyn
 */
public interface Command {
    /**
     * @return command name
     */
    String getName();

    /**
     * @return a list of areas for this command
     */
    List<Area> getAreaList();

    /**
     * Adds an area to this command
     * @param area
     * @return this command instance
     */
    Command addArea(Area area);

    /**
     * Applies a command at the given cell reference
     * @param cellRef cell reference where the command must be applied
     * @param context bean context to use
     * @return size of enclosing command area after transformation
     */
    Size applyAt(CellRef cellRef, Context context);

    /**
     * Resets command data for repeatable command usage
     */
    void reset();

}
