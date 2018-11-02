/*
 *
 *  Copyright 2017-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package springfox.documentation.spring.data.rest;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.rest.core.Path;
import org.springframework.data.rest.core.mapping.ResourceMapping;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.RequestHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static springfox.documentation.spring.data.rest.SpecificationBuilder.*;

public class EntityAssociationSaveExtractor implements EntityAssociationOperationsExtractor {

  @Override
  public List<RequestHandler> extract(EntityAssociationContext context) {

    List<RequestHandler> handlers = new ArrayList<>();

    Optional<ResourceMetadata> metadataOptional = context.associationMetadata();

    Association<? extends PersistentProperty<?>> association = context.getAssociation();

    PersistentProperty<?> property = association.getInverse();

    // ResourceMapping mapping = metadataOptional.orElse(null).getMappingFor(property);

    EntityContext entityContext = context.getEntityContext();

    String mappingPath = context.associationMetadata()
            .map(metadata -> metadata.getMappingFor(property))
            .map(ResourceMapping::getPath)
            .map(Path::toString)
            .orElse("");

    entityContext.entity()
            .filter(entity -> property.isWritable() && property.getOwner().equals(entity))
            .ifPresent(entity -> {
              String path = String.format("%s%s/{id}/%s",
                      context.getEntityContext().basePath(),
                      context.getEntityContext().resourcePath(),
                      mappingPath);
              associationAction(context, path)
                      .supportsMethod(RequestMethod.PUT)
                      .supportsMethod(RequestMethod.PATCH)
                      .supportsMethod(RequestMethod.POST)
                      .consumes(RestMediaTypes.TEXT_URI_LIST)
                      .consumes(RestMediaTypes.SPRING_DATA_COMPACT_JSON)
                      .withParameterType(ParameterType.ID)
                      .withParameterType(ParameterType.RESOURCE)
                      .build()
                      .map(update -> new SpringDataRestRequestHandler(context.getEntityContext(), update))
                      .ifPresent(handlers::add);
            });

    return handlers;
  }

}
