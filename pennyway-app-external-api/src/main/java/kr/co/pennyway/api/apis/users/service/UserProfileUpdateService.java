package kr.co.pennyway.api.apis.users.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.infra.client.aws.s3.AwsS3Provider;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import kr.co.pennyway.infra.common.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {
	private final PasswordEncoderHelper passwordEncoderHelper;
	private final AwsS3Provider awsS3Provider;

	@Transactional
	public void updateName(User user, String newName) {
		user.updateName(newName);
	}

	@Transactional
	public void updateUsername(User user, String newUsername) {
		user.updateUsername(newUsername);
	}

	@Transactional
	public void updatePassword(User user, String oldPassword, String newPassword) {
		if (passwordEncoderHelper.isSamePassword(user.getPassword(), newPassword)) {
			log.info("기존과 동일한 비밀번호로는 변경할 수 없습니다.");
			throw new UserErrorException(UserErrorCode.PASSWORD_NOT_CHANGED);
		}

		user.updatePassword(passwordEncoderHelper.encodePassword(newPassword));
	}

	@Transactional
	public void updateProfileImage(User user, String profileImageUrl) {
		// Profile Image 존재 여부 확인
		if (!awsS3Provider.doesObjectExist(profileImageUrl)) {
			log.info("프로필 이미지 URL이 유효하지 않습니다.");
			throw new StorageException(StorageErrorCode.NOT_FOUND);
		}

		// Profile Image 원본 저장
		awsS3Provider.copyObject(ObjectKeyType.PROFILE, profileImageUrl);

		// Profile Image URL 업데이트
		String originKey = ObjectKeyType.PROFILE.convertDeleteKeyToOriginKey(profileImageUrl);
		user.updateProfileImageUrl(getObjectPrefix() + originKey);
	}

	@Transactional
	public void updateNotifySetting(User user, NotifySetting.NotifyType type, Boolean flag) {
		user.getNotifySetting().updateNotifySetting(type, flag);
	}

	private String getObjectPrefix() {
		return "https://cdn.dev.pennyway.co.kr/";
	}
}
