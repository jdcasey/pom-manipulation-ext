/*
 * Copyright (C) 2012 Red Hat, Inc. (jcasey@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.ext.core.state;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.ext.common.ManipulationException;
import org.commonjava.maven.ext.core.impl.DependencyManipulator;
import org.commonjava.maven.ext.core.util.IdUtils;
import org.commonjava.maven.ext.core.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.commonjava.maven.ext.core.util.PropertiesUtils.getPropertiesByPrefix;

/**
 * Captures configuration relating to dependency alignment from the POMs. Used by {@link DependencyManipulator}.
 */
public class DependencyState
                implements State
{
    /**
     * The String that needs to be prepended a system property to make it a dependencyExclusion.
     * For example to exclude junit alignment for the GAV (org.groupId:artifactId)
     * <pre>
     * <code>-DdependencyExclusion.junit:junit@org.groupId:artifactId</code>
     * </pre>
     */
    private static final String DEPENDENCY_EXCLUSION_PREFIX = "dependencyExclusion.";

    /**
     * Defines how dependencies are located.
     */
    static final String DEPENDENCY_SOURCE = "dependencySource";

    /**
     * Merging precedence for dependency sources:
     * <pre>
     * <code>BOM</code> Solely Remote POM i.e. BOM.
     * <code>REST</code> Solely restURL.
     * <code>RESTBOM</code> Merges the information but takes the rest as precedence.
     * <code>BOMREST</code> Merges the information but takes the bom as precedence.
     * </pre>
     * Configured by the property <code>-DdependencySource=[REST|BOM|RESTBOM|BOMREST]</code>
     */
    public enum DependencyPrecedence
    {
        REST,
        BOM,
        RESTBOM,
        BOMREST
    }

    /**
     * Merely an alias for {@link DependencyState#DEPENDENCY_EXCLUSION_PREFIX}
     */
    private static final String DEPENDENCY_OVERRIDE_PREFIX = "dependencyOverride.";

    /**
     * The name of the property which contains the GAV of the remote pom from which to retrieve dependency management
     * information.
     * <pre>
     * <code>-DdependencyManagement:org.foo:bar-dep-mgmt:1.0</code>
     * </pre>
     */
    private static final String DEPENDENCY_MANAGEMENT_POM_PROPERTY = "dependencyManagement";

    private static final Logger logger = LoggerFactory.getLogger( DependencyState.class );

    private final boolean overrideDependencies;

    private final List<ProjectVersionRef> remoteBOMdepMgmt;

    private Map<String, String> dependencyExclusions;

    private Map<ArtifactRef, String> remoteRESTdepMgmt;

    private DependencyPrecedence precedence;

    public DependencyState( final Properties userProps ) throws ManipulationException
    {
        if ( userProps.containsKey( "overrideDependencies" ) )
        {
            logger.error ("overrideDependencies is deprecated.");
        }
        overrideDependencies = Boolean.valueOf( userProps.getProperty( "overrideDependencies", "true" ) );
        remoteBOMdepMgmt = IdUtils.parseGAVs( userProps.getProperty( DEPENDENCY_MANAGEMENT_POM_PROPERTY ) );
        dependencyExclusions = getPropertiesByPrefix( userProps, DEPENDENCY_EXCLUSION_PREFIX );

        Map<String, String> oP = PropertiesUtils.getPropertiesByPrefix( userProps, DEPENDENCY_OVERRIDE_PREFIX );
        for ( String s : oP.keySet() )
        {
            if ( dependencyExclusions.put( s, oP.get( s ) ) != null )
            {
                throw new ManipulationException( "Property clash between dependencyOverride and dependencyExclusion for " + s );
            }
        }
        switch ( DependencyPrecedence.valueOf( userProps.getProperty( DEPENDENCY_SOURCE,
                                                            DependencyPrecedence.BOM.toString() ).toUpperCase() ) )
        {
            case REST:
            {
                precedence = DependencyPrecedence.REST;
                break;
            }
            case BOM:
            {
                precedence = DependencyPrecedence.BOM;
                break;
            }
            case RESTBOM:
            {
                precedence = DependencyPrecedence.RESTBOM;
                break;
            }
            case BOMREST:
            {
                precedence = DependencyPrecedence.BOMREST;
                break;
            }
            default:
            {
                throw new ManipulationException( "Unknown value {} for {}", userProps.getProperty( DEPENDENCY_SOURCE ), DEPENDENCY_SOURCE);
            }
        }
    }

    /**
     * Enabled ONLY if dependencyManagement is provided OR restURL has been provided.
     *
     * @see org.commonjava.maven.ext.core.state.State#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        return ( ( remoteBOMdepMgmt != null && !remoteBOMdepMgmt.isEmpty()   ) ||
                 ( remoteRESTdepMgmt != null && !remoteRESTdepMgmt.isEmpty() ) ||
                 (!dependencyExclusions.isEmpty()) );
    }

    public List<ProjectVersionRef> getRemoteBOMDepMgmt()
    {
        return remoteBOMdepMgmt;
    }

    /**
     * @return whether to override managed dependencies in the build. Defaults to true.
     * @deprecated
     */
    public boolean getOverrideDependencies()
    {
        return overrideDependencies;
    }

    public DependencyPrecedence getPrecedence()
    {
        return precedence;
    }

    public void setRemoteRESTOverrides( Map<ArtifactRef, String> overrides )
    {
        remoteRESTdepMgmt = overrides;
    }

    public Map<ArtifactRef, String> getRemoteRESTOverrides( )
    {
        if ( remoteRESTdepMgmt == null )
        {
            remoteRESTdepMgmt = new HashMap<>(  );
        }
        return remoteRESTdepMgmt;
    }

    public Map<String, String> getDependencyExclusions( )
    {
        return dependencyExclusions;
    }

    public void updateExclusions (String key, String value)
    {
        dependencyExclusions.put( key, value );
    }
}
