package twins.logic;

import java.util.List;
import twins.boundaries.UserBoundary;

public interface ExtendedUsersService extends UsersService {

	public List<UserBoundary> getAllUsers(String adminSpace, String adminEmail, int size, int page);
}
