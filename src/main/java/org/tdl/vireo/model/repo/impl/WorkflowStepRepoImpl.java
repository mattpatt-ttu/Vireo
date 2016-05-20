package org.tdl.vireo.model.repo.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdl.vireo.model.Organization;
import org.tdl.vireo.model.WorkflowStep;
import org.tdl.vireo.model.repo.FieldProfileRepo;
import org.tdl.vireo.model.repo.OrganizationRepo;
import org.tdl.vireo.model.repo.WorkflowStepRepo;
import org.tdl.vireo.model.repo.custom.WorkflowStepRepoCustom;

public class WorkflowStepRepoImpl implements WorkflowStepRepoCustom {
    
    @PersistenceContext
    private EntityManager em;
	
    @Autowired
    private WorkflowStepRepo workflowStepRepo;
    
    @Autowired
    private FieldProfileRepo fieldProfileRepo;
    
    @Autowired
    private OrganizationRepo organizationRepo;
    
    @Override
    public WorkflowStep create(String name, Organization originatingOrganization) {
        WorkflowStep workflowStep = workflowStepRepo.save(new WorkflowStep(name, originatingOrganization));
        originatingOrganization.addWorkflowStep(workflowStep);
        
        //workflowStep.addContainingOrganization(originatingOrganization);
        
        return workflowStep;
    }
    
    // TODO: this method needs to handle all inheretence and aggregation duties
    public WorkflowStep update(WorkflowStep workflowStep, Organization requestingOrganization) {
        
        WorkflowStep originatingWorkflowStep = workflowStep.getOriginatingWorkflowStep();
        
        em.detach(workflowStep);
        
        workflowStep.setId(null);
        
        workflowStep.setOriginatingWorkflowStep(originatingWorkflowStep);
        
        return workflowStepRepo.save(workflowStep);
    }
    
    @Override
    public void delete(WorkflowStep workflowStep) {
        Organization originatingOrganization = workflowStep.getOriginatingOrganization();
        
        originatingOrganization.removeWorkflowStep(workflowStep);
        
        organizationRepo.save(originatingOrganization);
        
        fieldProfileRepo.findByOriginatingWorkflowStep(workflowStep).forEach(fieldProfile -> {
        	fieldProfileRepo.delete(fieldProfile);
        });
        
        workflowStepRepo.delete(workflowStep.getId());
    }
    
}
