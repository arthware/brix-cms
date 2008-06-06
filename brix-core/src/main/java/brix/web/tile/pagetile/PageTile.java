package brix.web.tile.pagetile;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.model.IModel;

import brix.Path;
import brix.jcr.api.JcrNode;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.page.AbstractContainer;
import brix.plugin.site.page.tile.Tile;
import brix.plugin.site.page.tile.admin.TileEditorPanel;
import brix.web.nodepage.BrixPageParameters;

public class PageTile implements Tile
{
    public static String TYPE_NAME = PageTile.class.getName();

    public PageTile()
    {
    }

    public String getDisplayName()
    {
        return "Page Tile";
    }

    public String getTypeName()
    {
        return TYPE_NAME;
    }

    public TileEditorPanel newEditor(String id, IModel<BrixNode> containerNode)
    {
        return new PageTileEditorPanel(id, containerNode);
    }

    public Component newViewer(String id, IModel<BrixNode> tileNode,
            BrixPageParameters pageParameters)
    {
        return new PageTileViewerPanel(id, tileNode);
    }

    // needed to detect loop during #requiresSSL call
    private static final MetaDataKey<Set<Path>> NODE_SET_KEY = new MetaDataKey<Set<Path>>()
    {
    };

    public boolean requiresSSL(IModel<BrixNode> tileNode)
    {

        // get or create set of paths that were already processed
        Set<Path> set = (Set<Path>)RequestCycle.get().getMetaData(NODE_SET_KEY);
        if (set == null)
        {
            set = new HashSet<Path>();
            RequestCycle.get().setMetaData(NODE_SET_KEY, set);
        }


        Path nodePath = new Path(tileNode.getObject().getParent().getPath());

        if (set.contains(nodePath))
        {
            // this means we found a loop. However here we just return false,
            // PageTileViewerPanel is responsible for displaying the error
            return false;
        }
        set.add(nodePath);

        final boolean result;

        if (tileNode.getObject().hasProperty("pageNode"))
        {
            JcrNode pageNode = tileNode.getObject().getProperty("pageNode").getNode();
            result = ((AbstractContainer)pageNode).requiresSSL();
        }
        else
        {
            result = false;
        }


        set.remove(nodePath);
        if (set.isEmpty())
        {
            RequestCycle.get().setMetaData(NODE_SET_KEY, null);
        }

        return result;
    }

}
