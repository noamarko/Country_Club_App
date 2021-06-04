package twins.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import twins.data.UserEntity;
import twins.dal.UserHandler;
import twins.additionalClasses.UserId;
import twins.boundaries.UserBoundary;


@Service
public class UserServiceImplementation implements ExtendedUsersService {
	private UserHandler userHandler;
	private String space;
	
	@Autowired	
	public UserServiceImplementation(UserHandler userHandler) {
		super();
		this.userHandler = userHandler;
	}
	
	@Value("${spring.application.name:2021b.integ}")
	public void setSpace(String space) {
		this.space = space;
	}
	
	@PostConstruct
	public void init() {
		
	}
	
	@Override
	@Transactional //(readOnly = false)
	public UserBoundary createUser(UserBoundary boundary) {
		if (boundary.getUsername()==null) {
			throw new RuntimeException("UserName attribute must not be null");
		}
		if (boundary.getAvatar()==null) {
			throw new RuntimeException("Avatar attribute must not be null");
		}
		if (boundary.getAvatar()=="") {
			throw new RuntimeException("Avatar attribute must not be an empty string");
		}
		if (!(boundary.getUserId().getEmail().contains("@"))) {
			throw new RuntimeException("Email attribute is not valid");
		}
		UserEntity entity = this.convertToEntity(boundary);
		entity = this.userHandler.save(entity);
				
		return this.convertToBoundary(entity);
	}

	@Override											//maybe required catching the exception
	@Transactional(readOnly = true)
	public UserBoundary login(String userSpace, String userEmail) {
		Optional<UserEntity> ue = this.userHandler.findById(userSpace + "|" + userEmail); 
		UserBoundary ub = new UserBoundary();
		if (ue.isPresent()) {
			ub.setUserId(new UserId(userSpace,userEmail));
			ub.setRole(ue.get().getRole());
			ub.setUserName(ue.get().getUsername());
			ub.setAvatar(ue.get().getAvatar());
		}
		else {
			throw new RuntimeException("user could not be found");
		}
		return ub;
	}

	@Override
	@Transactional //(readOnly = false)
	public UserBoundary updateUser(String userSpace, String userEmail, UserBoundary update) {
		Optional<UserEntity> oue = this.userHandler.findById(update.getUserId().getSpace()+ "|" + update.getUserId().getEmail());
		if (oue.isPresent()) {	//updating existing user.
			update.setUserId(userSpace, userEmail);
			UserEntity updatedEntity = this.convertToEntity(update);	
			this.userHandler.save(updatedEntity);
		}
		else {		//user doesn't exist
			throw new RuntimeException("user could not be found");
		}
		return update;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllUsers(String adminSpace, String adminEmail, int size, int page) {
		Page<UserEntity> entitiesPage = this.userHandler.findAll(PageRequest.of(page, size, Direction.DESC, "UserId"));
		List<UserEntity> content = entitiesPage.getContent(); 
		List<UserBoundary> boundaries = new ArrayList<>();
		Optional<UserEntity> oue = this.userHandler.findById(adminSpace + "|" + adminEmail);
		if (oue.isPresent()) {							// Check if user exists
			if (oue.get().getRole().equals("ADMIN")) {	// Check for admin permissions
				for (UserEntity entity : content) 
					boundaries.add(this.convertToBoundary(entity));
			}
		}
		return boundaries;
	}

	@Override
	@Transactional //(readOnly = false)
	public void deleteAllUsers(String adminSpace, String adminEmail) {
		this.userHandler.deleteAll();
		
	}
	
	public UserEntity convertToEntity(UserBoundary boundary) {
		UserEntity entity = new UserEntity();
		entity.setUserID(boundary.getUserId());
		entity.setUserID(new UserId(space, boundary.getUserId().getEmail()));
		entity.setRole(boundary.getRole().toString());
		entity.setUsername(boundary.getUsername());
		entity.setAvatar(boundary.getAvatar());

		return entity;
	}
	
	private UserBoundary convertToBoundary(UserEntity entity) {
		UserBoundary boundary = new UserBoundary();
		String[] tokens = getTokens(entity.getUserId());
		boundary.setUserId(new UserId(tokens[0], tokens[1]));
		boundary.setRole(entity.getRole());
		boundary.setUserName(entity.getUsername());
		boundary.setAvatar(entity.getAvatar());
		
		return boundary;
	}
	
	private String[] getTokens(String userID) {
		String[] tokens = new String[2];
		tokens = userID.split("\\|");
		return tokens;
	}

	@Override
	@Transactional(readOnly = true) // handle race condition
	@Deprecated
	public List<UserBoundary> getAllUsers(String adminSpace, String adminEmail) {
		throw new RuntimeException("deprecated operation");
	}
	
}
