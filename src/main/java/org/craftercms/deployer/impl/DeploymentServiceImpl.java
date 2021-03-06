/*
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.deployer.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.deployer.api.DeploymentContext;
import org.craftercms.deployer.api.DeploymentResolver;
import org.craftercms.deployer.api.DeploymentService;
import org.craftercms.deployer.api.exceptions.DeploymentException;
import org.craftercms.deployer.api.results.DeploymentFailure;
import org.craftercms.deployer.api.results.DeploymentResult;
import org.craftercms.deployer.api.results.DeploymentSuccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by alfonsovasquez on 30/11/16.
 */
@Component("deploymentService")
public class DeploymentServiceImpl implements DeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    protected final DeploymentResolver deploymentResolver;

    @Autowired
    public DeploymentServiceImpl(DeploymentResolver deploymentResolver) {
        this.deploymentResolver = deploymentResolver;
    }

    @Override
    public  List<DeploymentResult> deployAllSites() throws DeploymentException {
        List<DeploymentContext> deploymentContexts = deploymentResolver.resolveAll();
        List<DeploymentResult> results = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(deploymentContexts)) {
            deploymentContexts.forEach(context -> deploySite(context, results));
        }

        return results;
    }

    protected void deploySite(DeploymentContext context, List<DeploymentResult> results) {
        try {
            context.getDeploymentPipeline().execute(context);

            results.add(new DeploymentSuccess(context.getId()));

            logger.info("Deployment of '{}' successful", context.getId());
        } catch (Exception e) {
            results.add(new DeploymentFailure(context.getId(), e.toString()));

            logger.error("Deployment of '" + context.getId() + "' failed", e);

            context.getErrorHandler().onError(context, e);
        }
    }

}
