package brix.plugin.site.page.admin;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import brix.jcr.api.JcrNode;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.SiteNavigationTreeNode;
import brix.plugin.site.admin.NodeManagerPanel;
import brix.plugin.site.page.AbstractContainer;
import brix.plugin.site.page.Page;
import brix.plugin.site.page.PageSiteNodePlugin;
import brix.plugin.site.page.Template;
import brix.web.ContainerFeedbackPanel;
import brix.web.admin.navigation.NavigationTreeNode;
import brix.web.util.validators.NodeNameValidator;

public class CreateTilePagePanel extends NodeManagerPanel
{

    private String name;

    public CreateTilePagePanel(String id, IModel<BrixNode> containerNodeModel, final String type)
    {
        super(id, containerNodeModel);


        add(new Label("label", "Create New " + type + ":"));

        add(new ContainerFeedbackPanel("feedback", this));
        Form form = new Form("form", new CompoundPropertyModel(this))
        {
            protected void onSubmit()
            {
                createPage(type);
            };

        };
        add(form);

        final TextField tf;
        form.add(tf = new TextField("name"));
        tf.setRequired(true);
        tf.add(NodeNameValidator.getInstance());

    }

    private void createPage(String type)
    {
        final JcrNode parent = getNode();

        if (parent.hasNode(name))
        {
            error("Resource with name '" + name + "' already exists");
        }
        else
        {

            JcrNode page = parent.addNode(name, "nt:file");

            AbstractContainer node;

            if (type.equals(PageSiteNodePlugin.TYPE))
            {
                node = Page.initialize(page);
            }
            else
            {
                node = Template.initialize(page);
            }

            node.setData("");
            name = null;
            
            parent.save();

            NavigationTreeNode treeNode = new SiteNavigationTreeNode(node);
            getNavigation().nodeInserted(treeNode);
            getNavigation().selectNode(treeNode);            
        }
    }

}
