package com.skladanov.VseSvoiSearchBot.bot;

import com.skladanov.VseSvoiSearchBot.bot.model.User;
import com.skladanov.VseSvoiSearchBot.bot.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {
    private final UserRepository userRepository;
    private static final String START = "/start";
    private static final String REQUEST = "/request";
    private static final String DELETE = "/delete";

    @Override
    public SendMessage makeAnswer(Update update) {
        User currentUser = getUserOrSave(update);
        String message = update.getMessage().getText().stripLeading();
        if(message.charAt(0) == '/') {
            return checkCommand(message, currentUser.getId(), currentUser);
        }
        if (currentUser.getIsCreationRequest()) {
            return makeUserRequest(message, currentUser);
        }
        return unknownCommand(currentUser.getId());
    }

    private User getUserOrSave(Update update) {
        Long chatId = update.getMessage().getChatId();
        Optional<User> userOptional = userRepository.findById(chatId);
        User user = null;
        if (userOptional.isEmpty()) {
            user = new User(chatId, update.getMessage().getChat().getUserName());
            userRepository.save(user);
        } else {
            user = userOptional.get();
        }
        return user;
    }

    private SendMessage checkCommand(String message, Long chatId, User currentUser) {
        switch (message) {
            case START -> {
                return startCommand(chatId, currentUser.getName());
            }
            case DELETE -> {
                return makeSendMessage(chatId, "Запрос удален. Чтобы составить новый запрос, " +
                        "введите команду \"/request\"");
            }
            case REQUEST -> {
                if(currentUser.getIsCreationRequest()) {
                    var text = "Вы уже в процессе заполнения запроса на поиск. " +
                               "Если хотите удалить текущий запрос, введите команду \"/delete\"";
                    return makeSendMessage(chatId, text);
                }
                currentUser.setIsCreationRequest(true);
                var text = "Отлично! Сейчас я попрошу вас заполнить несколько полей, " +
                            "чтобы составить запрос, после чего вы сможете подтвердить отправку. " +
                            "Если вы не хотите или не знаете, что написать в поле, " +
                            "просто поставьте прочерк(-) или любой другой символ и переходите к следующему вопросу. " +
                            "Итак, для начала введите требования к возрасту специалиста: ";
                currentUser.setStage(RequestStages.SPECIALIST_GENDER);
                return makeSendMessage(chatId, text);
                }
            default -> {
                return unknownCommand(chatId);
            }
        }
    }

    public SendMessage makeUserRequest(String message, User currentUser) {
        String text = switch (currentUser.getStage()) {
            case SPECIALIST_AGE -> {
                currentUser.setStage(RequestStages.SPECIALIST_GENDER);
                yield "Пожалуйста, введите требования к возрасту специалиста: ";
            }
            case SPECIALIST_GENDER -> {
                currentUser.setStage(RequestStages.METHOD);
                yield "Пожалуйста, введите требования к полу специалиста: ";
            }
            case METHOD -> {
                currentUser.setStage(RequestStages.BUDGET);
                yield "Пожалуйста, введите требования к методу, в котором работает специалист";
            }
            case BUDGET -> {
                currentUser.setStage(RequestStages.CLIENT_AGE);
                yield "Пожалуйста, введите ваши пожелания касаемо стоимости сессий и их частоты";
            }
            case CLIENT_AGE -> {
                currentUser.setStage(RequestStages.CLIENT_GENDER);
                yield "Пожалуйста, введите ваш возраст: ";
            }
            case CLIENT_GENDER -> {
                currentUser.setStage(RequestStages.CLIENT_DIAGNOSIS);
                yield "Пожалуйста, введите ваш пол: ";
            }
            case CLIENT_DIAGNOSIS -> {
                currentUser.setStage(RequestStages.CLIENT_REQUEST);
                yield "Пожалуйста, укажите психиатрические диагнозы, " +
                        "которые были поставлены психиатром или которые вы у себя подозреваете. " +
                        "А так же прочие, если они связаны с ваши запросом: ";
            }
            case CLIENT_REQUEST -> {
                currentUser.setStage(RequestStages.EXTRA);
                yield "Пожалуйста, опишите ваш запрос (например: \"Психиатр поставил мне эти диагнозы и рекомендовал обратиться к КПТ-психологу\" \n" +
                        "\"Я хочу уменьшить выраженность депрессии\" \n" +
                        "\"Я переживаю развод и мне нужна помощь\"):";
            }
            case EXTRA -> {
                currentUser.setStage(RequestStages.SEND_REQUEST);
                yield "Здесь вы можете указать дополнительные пожелания к специалисту, " +
                        "другие сведения о себе и любую информацию, " +
                        "которую посчитаете необходимой: ";
            }
            case SEND_REQUEST -> {
                currentUser.setStage(RequestStages.SEND_APPROVED);
                yield "Отправить запрос?";
            }
            case SEND_APPROVED -> {
                if (message.equals("/send")) {
                    currentUser.setIsCreationRequest(false);
                    yield "Запрос отправлен!";
                } else {
                    yield "Команда не распознана. Чтобы отправить запрос, введите \"/send\". " +
                            "Чтобы удалить запрос, введите /delete";
                }
            }
        };
        return makeSendMessage(currentUser.getId(), text);
    }

    private SendMessage startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в бот, %s!
                                
                Здесь Вы сможете найти специалиста из сообщества "Все свои" под свой запрос.
                Для этого воспользуйтесь командой:
                /request - начать составление запроса
                                
                Дополнительные команды:
     
                """;
        return makeSendMessage(chatId, String.format(text, userName));
    }

    private SendMessage unknownCommand(Long chatId) {
        var text = "Не удалось распознать команду!";
        return makeSendMessage(chatId, text);
    }


    private SendMessage makeSendMessage(Long chatId, String text) {
        return new SendMessage(String.valueOf(chatId), text);
    }
}