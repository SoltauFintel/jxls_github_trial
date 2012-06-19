package com.jxls.writer.command;

import com.jxls.writer.area.Area;
import com.jxls.writer.common.CellRef;
import com.jxls.writer.common.Context;
import com.jxls.writer.common.Size;
import com.jxls.writer.expression.ExpressionEvaluator;
import com.jxls.writer.expression.JexlExpressionEvaluator;
import com.jxls.writer.util.Util;

import java.util.Collection;

/**
 * Implements iteration over collection of items
 * 'items' is a bean name of the collection in context
 * 'var' is a name of a collection item to put into the context during the iteration
 * 'direction' defines expansion by rows (DOWN) or by columns (RIGHT). Default is DOWN.
 * 'cellRefGenerator' defines custom strategy for target cell references.
 * Date: Nov 10, 2009
 * @author Leonid Vysochyn
 */
public class EachCommand extends AbstractCommand {
    public enum Direction {RIGHT, DOWN}
    
    String var;
    String items;
    String select;
    Area area;
    Direction direction = Direction.DOWN;
    CellRefGenerator cellRefGenerator;

    public EachCommand() {
    }

    /**
     * @param var name of the key in the context to contain each collection items during iteration
     * @param items name of the collection bean in the context
     * @param direction defines processing by rows (DOWN - default) or columns (RIGHT)
     */
    public EachCommand(String var, String items, Direction direction) {
        this.var = var;
        this.items = items;
        this.direction = direction == null ? Direction.DOWN : direction;
    }

    public EachCommand(String var, String items, Area area) {
        this(var, items, area, Direction.DOWN);
    }
    
    public EachCommand(String var, String items, Area area, Direction direction) {
        this( var, items, direction );
        if( area != null ){
            this.area = area;
            addArea(this.area);
        }
    }

    /**
     *
     * @param var name of the key in the context to contain each collection items during iteration
     * @param items name of the collection bean in the context
     * @param area body area for this command
     * @param cellRefGenerator generates target cell ref for each collection item during iteration
     */
    public EachCommand(String var, String items, Area area, CellRefGenerator cellRefGenerator) {
        this(var, items, area, (Direction)null );
        this.cellRefGenerator = cellRefGenerator;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public CellRefGenerator getCellRefGenerator() {
        return cellRefGenerator;
    }

    public void setCellRefGenerator(CellRefGenerator cellRefGenerator) {
        this.cellRefGenerator = cellRefGenerator;
    }

    public String getName() {
        return "each";
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    @Override
    public Command addArea(Area area) {
        if( area == null ){
            return this;
        }
        if( areaList.size() >= 1){
            throw new IllegalArgumentException("You can add only a single area to 'each' command");
        }
        this.area = area;
        return super.addArea(area);
    }

    public Size applyAt(CellRef cellRef, Context context) {
        Collection itemsCollection = calculateItemsCollection(context);
        int width = 0;
        int height = 0;
        int index = 0;
        CellRef currentCell = cellRefGenerator != null ? cellRefGenerator.generateCellRef(index, context) : cellRef;
        JexlExpressionEvaluator selectEvaluator = null;
        if( select != null ){
            selectEvaluator = new JexlExpressionEvaluator( select );
        }
        for( Object obj : itemsCollection){
            context.putVar(var, obj);
            if( selectEvaluator != null ){
                if( !Util.isConditionTrue( selectEvaluator, context )) {
                    context.removeVar(var);
                    continue;
                }
            }
            Size size = area.applyAt(currentCell, context);
            index++;
            if( cellRefGenerator != null ){
                width = Math.max(width, size.getWidth());
                height = Math.max(height, size.getHeight());
                currentCell = cellRefGenerator.generateCellRef(index, context);
            }else if( direction == Direction.DOWN ){
                currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow() + size.getHeight(), currentCell.getCol());
                width = Math.max(width, size.getWidth());
                height += size.getHeight();
            }else{
                currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
                width += size.getWidth();
                height = Math.max( height, size.getHeight() );
            }
            context.removeVar(var);
        }
        return new Size(width, height);
    }

    protected Collection calculateItemsCollection(Context context){
        ExpressionEvaluator expressionEvaluator = new JexlExpressionEvaluator(context.toMap());
        Object itemsObject = expressionEvaluator.evaluate(items);
        if( !(itemsObject instanceof Collection) ){
            throw new RuntimeException("items expression is not a collection");
        }
        return (Collection) itemsObject;
    }
}
