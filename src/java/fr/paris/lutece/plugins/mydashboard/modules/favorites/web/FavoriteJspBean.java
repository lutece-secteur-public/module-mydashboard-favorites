/*
 * Copyright (c) 2002-2016, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.mydashboard.modules.favorites.web;

import fr.paris.lutece.plugins.mydashboard.modules.favorites.business.Favorite;
import fr.paris.lutece.plugins.mydashboard.modules.favorites.business.FavoriteHome;
import fr.paris.lutece.plugins.mydashboard.modules.favorites.service.FavoriteService;
import fr.paris.lutece.plugins.mydashboard.service.IMyDashboardComponent;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginDefaultImplementation;
import fr.paris.lutece.portal.service.plugin.PluginEvent;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to manage Favorite features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageFavorites.jsp", controllerPath = "jsp/admin/plugins/mydashboard/modules/favorites/", right = "FAVORITES_MANAGEMENT" )
public class FavoriteJspBean extends ManageFavoritesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_FAVORITES = "/admin/plugins/mydashboard/modules/favorites/manage_favorites.html";
    private static final String TEMPLATE_CREATE_FAVORITE = "/admin/plugins/mydashboard/modules/favorites/create_favorite.html";
    private static final String TEMPLATE_MODIFY_FAVORITE = "/admin/plugins/mydashboard/modules/favorites/modify_favorite.html";

    // Parameters
    private static final String PARAMETER_ID_FAVORITE = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_FAVORITES = "module.mydashboard.favorites.manage_favorites.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_FAVORITE = "module.mydashboard.favorites.modify_favorite.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_FAVORITE = "module.mydashboard.favorites.create_favorite.pageTitle";

    // Markers
    private static final String MARK_FAVORITE_LIST = "favorite_list";
    private static final String MARK_FAVORITE = "favorite";

    private static final String JSP_MANAGE_FAVORITES = "jsp/admin/plugins/mydashboard/modules/favorites/ManageFavorites.jsp";
    
    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_FAVORITE = "module.mydashboard.favorites.message.confirmRemoveFavorite";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "favorites.model.entity.favorite.attribute.";

    // Views
    private static final String VIEW_MANAGE_FAVORITES = "manageFavorites";
    private static final String VIEW_CREATE_FAVORITE = "createFavorite";
    private static final String VIEW_MODIFY_FAVORITE = "modifyFavorite";

    // Actions
    private static final String ACTION_CREATE_FAVORITE = "createFavorite";
    private static final String ACTION_MODIFY_FAVORITE = "modifyFavorite";
    private static final String ACTION_REMOVE_FAVORITE = "removeFavorite";
    private static final String ACTION_CONFIRM_REMOVE_FAVORITE = "confirmRemoveFavorite";
    private static final String ACTION_TOGGLE_ACTIVATION_FAVORITE = "toggleActivationFavorite";

    // Infos
    private static final String INFO_FAVORITE_CREATED = "module.mydashboard.favorites.info.favorite.created";
    private static final String INFO_FAVORITE_UPDATED = "module.mydashboard.favorites.info.favorite.updated";
    private static final String INFO_FAVORITE_REMOVED = "module.mydashboard.favorites.info.favorite.removed";
    
    // Session variable to store working values
    private Favorite _favorite;
    
    /**
     * Build the Manage View
     * @param request The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_FAVORITES, defaultView = true )
    public String getManageFavorites( HttpServletRequest request )
    {
        _favorite = null;
        List<Favorite> listFavorites = FavoriteHome.getFavoritesList(  );
        Map<String, Object> model = getPaginatedListModel( request, MARK_FAVORITE_LIST, listFavorites, JSP_MANAGE_FAVORITES );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_FAVORITES, TEMPLATE_MANAGE_FAVORITES, model );
    }

    /**
     * Returns the form to create a favorite
     *
     * @param request The Http request
     * @return the html code of the favorite form
     */
    @View( VIEW_CREATE_FAVORITE )
    public String getCreateFavorite( HttpServletRequest request )
    {
        _favorite = ( _favorite != null ) ? _favorite : new Favorite(  );

        Map<String, Object> model = getModel(  );
        model.put( MARK_FAVORITE, _favorite );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_FAVORITE, TEMPLATE_CREATE_FAVORITE, model );
    }

    /**
     * Process the data capture form of a new favorite
     *
     * @param request The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_FAVORITE )
    public String doCreateFavorite( HttpServletRequest request )
    {
        populate( _favorite, request );

        // Check constraints
        if ( !validateBean( _favorite, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_FAVORITE );
        }

        FavoriteHome.create( _favorite );
        addInfo( INFO_FAVORITE_CREATED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_FAVORITES );
    }

    /**
     * Manages the removal form of a favorite whose identifier is in the http
     * request
     *
     * @param request The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_FAVORITE )
    public String getConfirmRemoveFavorite( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_FAVORITE ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_FAVORITE ) );
        url.addParameter( PARAMETER_ID_FAVORITE, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_FAVORITE, url.getUrl(  ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a favorite
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage favorites
     */
    @Action( ACTION_REMOVE_FAVORITE )
    public String doRemoveFavorite( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_FAVORITE ) );
        FavoriteHome.remove( nId );
        addInfo( INFO_FAVORITE_REMOVED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_FAVORITES );
    }

    /**
     * Returns the form to update info about a favorite
     *
     * @param request The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_FAVORITE )
    public String getModifyFavorite( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_FAVORITE ) );

        if ( _favorite == null || ( _favorite.getId(  ) != nId ))
        {
            _favorite = FavoriteHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel(  );
        model.put( MARK_FAVORITE, _favorite );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_FAVORITE, TEMPLATE_MODIFY_FAVORITE, model );
    }

    /**
     * Process the change form of a favorite
     *
     * @param request The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_FAVORITE )
    public String doModifyFavorite( HttpServletRequest request )
    {
        populate( _favorite, request );

        // Check constraints
        if ( !validateBean( _favorite, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_FAVORITE, PARAMETER_ID_FAVORITE, _favorite.getId( ) );
        }

        FavoriteHome.update( _favorite );
        addInfo( INFO_FAVORITE_UPDATED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_FAVORITES );
    }
    
    /**
     * Toggle the activation of a favorite
     *
     * @param request The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_TOGGLE_ACTIVATION_FAVORITE )
    public String doToggleActivationFavorite( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_FAVORITE ) );
        Favorite favorite = FavoriteService.getInstance( ).findByPrimaryKey( nId );
        favorite.setIsActivated( ( favorite.getIsActivated( ) ) ? false : true );

        FavoriteHome.update( favorite );
        addInfo( INFO_FAVORITE_UPDATED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_FAVORITES );
    }
}