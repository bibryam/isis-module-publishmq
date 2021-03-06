/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.module.publishmq.fixcanonicalmappings;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.schema.common.v1_0.OidDto;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.service.conmap.ContentMappingService;

import org.isisaddons.module.publishmq.canonical.demoobject.DemoObjectDto;
import org.isisaddons.module.publishmq.fixture.dom.PublishMqDemoObject;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class PublishMqDemoContentMappingService implements ContentMappingService {

    private MapperFactory mapperFactory;

    @PostConstruct
    public void init() {
        mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.registerClassMap(
                mapperFactory.classMap(PublishMqDemoObject.class, DemoObjectDto.class)
                        .byDefault() // all fields are the compatible
                        .toClassMap());
        mapperFactory.registerClassMap(
                mapperFactory.classMap(Bookmark.class, OidDto.class)
                        .field("identifier", "objectIdentifier") // customized
                        .byDefault() // all other fields are compatible
                        .toClassMap());
    }

    @Programmatic
    @Override
    public Object map(
            final Object object,
            final List<MediaType> acceptableMediaTypes,
            final RepresentationType representationType) {

        if(object instanceof PublishMqDemoObject) {
            final Bookmark bookmark = bookmarkService.bookmarkFor(object);

            final DemoObjectDto dto = mapperFactory.getMapperFacade().map(object, DemoObjectDto.class);
            final OidDto oidDto = mapperFactory.getMapperFacade().map(bookmark, OidDto.class);

            // manually wire together
            dto.setOid(oidDto);

            return dto;
        }

        return null;
    }

    //region > injected services
    @javax.inject.Inject
    private BookmarkService bookmarkService;
    //endregion

}
