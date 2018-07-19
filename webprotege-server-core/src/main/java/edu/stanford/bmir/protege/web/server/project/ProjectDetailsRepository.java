package edu.stanford.bmir.protege.web.server.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import edu.stanford.bmir.protege.web.server.persistence.Repository;
import edu.stanford.bmir.protege.web.shared.inject.ProjectSingleton;
import edu.stanford.bmir.protege.web.shared.project.ProjectDetails;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.server.project.ProjectDetailsConverter.*;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11/03/16
 */
public class ProjectDetailsRepository implements Repository {

    public static final String COLLECTION_NAME = "ProjectDetails";

    @Nonnull
    private final ObjectMapper objectMapper;

    private final MongoCollection<Document> collection;

    @Inject
    public ProjectDetailsRepository(@Nonnull MongoDatabase database,
                                    @Nonnull ProjectDetailsConverter converter,
                                    @Nonnull ObjectMapper objectMapper) {
        this.collection = database.getCollection(COLLECTION_NAME);
        this.objectMapper = checkNotNull(objectMapper);
    }

    @Override
    public void ensureIndexes() {
        collection.createIndex(new Document(PROJECT_ID, 1).append(DISPLAY_NAME, 1));
    }

    public boolean containsProject(@Nonnull ProjectId projectId) {
        return collection
                .find(withProjectId(projectId))
                .projection(new Document())
                .limit(1)
                .first() != null;
    }

    public boolean containsProjectWithOwner(@Nonnull ProjectId projectId, @Nonnull UserId owner) {
        return collection
                .find(withProjectIdAndWithOwner(projectId, owner))
                .projection(new Document())
                .limit(1)
                .first() != null;
    }

    public void setInTrash(ProjectId projectId, boolean inTrash) {
        collection.updateOne(withProjectId(projectId), updateInTrash(inTrash));
    }

    public void setModified(ProjectId projectId, long modifiedAt, UserId modifiedBy) {
        collection.updateOne(withProjectId(projectId), updateModified(modifiedBy, modifiedAt));
    }

    public Optional<ProjectDetails> findOne(@Nonnull ProjectId projectId) {
        return Optional.ofNullable(
                collection
                        .find(withProjectId(projectId))
                        .limit(1)
                        .first())
                .map(d -> objectMapper.convertValue(d, ProjectDetails.class));
    }

    public List<ProjectDetails> findByOwner(UserId owner) {
        ArrayList<ProjectDetails> result = new ArrayList<>();
        collection.find(withOwner(owner))
                .map(d -> objectMapper.convertValue(d, ProjectDetails.class))
                .into(result);
        return result;
    }

    public void save(@Nonnull ProjectDetails projectRecord) {
        Document document = objectMapper.convertValue(projectRecord, Document.class);
        collection.replaceOne(withProjectId(projectRecord.getProjectId()),
                             document,
                             new UpdateOptions().upsert(true));
    }

    public void delete(@Nonnull ProjectId projectId) {
        collection.deleteOne(withProjectId(projectId));
    }
}
