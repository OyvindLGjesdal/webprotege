package edu.stanford.bmir.protege.web.server.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtCompatible;
import edu.stanford.bmir.protege.web.shared.form.FormId;
import edu.stanford.bmir.protege.web.shared.match.criteria.EntityMatchCriteria;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-08
 */
@GwtCompatible(serializable = true)
@AutoValue
public abstract class FormSelector {

    @JsonCreator
    public static FormSelector get(@Nonnull @JsonProperty("projectId") ProjectId projectId,
                                   @Nonnull @JsonProperty("criteria") EntityMatchCriteria criteria,
                                   @Nonnull @JsonProperty("formId") FormId formId) {
        return new AutoValue_FormSelector(projectId, criteria, formId);
    }

    @JsonProperty("projectId")
    public abstract ProjectId getProjectId();

    @JsonProperty("criteria")
    public abstract EntityMatchCriteria getCriteria();

    @JsonProperty("formId")
    public abstract FormId getFormId();

}
