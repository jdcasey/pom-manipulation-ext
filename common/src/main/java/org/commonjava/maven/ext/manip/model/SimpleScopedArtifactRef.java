/**
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
package org.commonjava.maven.ext.manip.model;

import org.apache.maven.model.Dependency;
import org.commonjava.maven.atlas.ident.DependencyScope;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleArtifactRef;
import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;
import org.commonjava.maven.atlas.ident.version.VersionSpec;

/**
 * Wrapper around SimpleArtifactRef to also store the scope of the dependency.
 */
public class SimpleScopedArtifactRef extends SimpleArtifactRef
{

    private final boolean optional;
    private final DependencyScope scope;

    public SimpleScopedArtifactRef( final String groupId, final String artifactId, final VersionSpec version,
                              final String type, final String classifier, final boolean optional,
                              final String scope)
    {
        super( groupId, artifactId, version, type, classifier );
        this.optional = optional;
        this.scope = DependencyScope.getScope( scope );
    }

    public SimpleScopedArtifactRef( final ProjectVersionRef ref, final TypeAndClassifier tc, final boolean optional, final String scope )
    {
        super( ref, tc );
        this.optional = optional;
        this.scope = DependencyScope.getScope( scope );
    }

    public SimpleScopedArtifactRef( ArtifactRef ref, boolean optional, DependencyScope scope )
    {
        super( ref, ref.getTypeAndClassifier() );
        this.optional = optional;
        this.scope = scope;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public String getScope()
    {
        return scope.realName();
    }
}
