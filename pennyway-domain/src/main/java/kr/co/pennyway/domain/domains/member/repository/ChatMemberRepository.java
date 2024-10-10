package kr.co.pennyway.domain.domains.member.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;

public interface ChatMemberRepository extends ExtendedRepository<ChatMember, Long>, CustomChatMemberRepository {
}
