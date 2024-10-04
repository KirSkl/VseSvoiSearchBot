package com.skladanov.VseSvoiSearchBot.bot;

import com.skladanov.VseSvoiSearchBot.bot.model.Request;
import com.skladanov.VseSvoiSearchBot.bot.model.User;
import com.skladanov.VseSvoiSearchBot.bot.repo.RequestRepository;
import com.skladanov.VseSvoiSearchBot.bot.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {
    private static final String HELP = "/help";
    private static final String REQUEST = "/request";
    private static final String DELETE = "/delete";
    private static final String BACK = "/back";
    private static final int BACK_INDEX = 2; //чтобы вернуться на шаг назад, нужно изменить состояние запроса на 2 значения назад
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    @Override
    public SendMessage makeAnswer(Update update) {
        User currentUser = getUserOrSave(update);
        String message = update.getMessage().getText().stripLeading();
        if (message.charAt(0) == '/') {
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
            user = new User(chatId);
            userRepository.save(user);
        } else {
            user = userOptional.get();
        }
        return user;
    }

    private SendMessage checkCommand(String message, Long chatId, User currentUser) {
        switch (message) {
            case HELP -> {
                return helpCommand(chatId);
            }
            case DELETE -> {
                final var request = getRequest(currentUser);
                if (request.isPresent()) {
                    requestRepository.delete(request.get());
                    return makeSendMessage(chatId, "Запрос удален. Чтобы составить новый запрос, " +
                            "введите команду \"/request\"");
                }
                return makeSendMessage(chatId, "На данный момент у вас нет сохраненного запроса. " +
                        "Чтобы составить новый запрос, введите команду \"/request\"");
            }
            case REQUEST -> {
                if (currentUser.getIsCreationRequest()) {
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
                userRepository.save(currentUser);
                return makeSendMessage(chatId, text);
            }
            case BACK -> {
                if (!currentUser.getIsCreationRequest() ||
                        currentUser.getStage().equals(RequestStages.SPECIALIST_GENDER) ||
                        currentUser.getStage().equals(RequestStages.SPECIALIST_AGE)) {
                    var text = "На данный момент у вас нет заполненных полей запроса";
                    return makeSendMessage(chatId, text);
                }
                var ListStages = Arrays.asList(RequestStages.values());
                var newStageIndex = ListStages.indexOf(currentUser.getStage()) - BACK_INDEX; //
                currentUser.setStage(ListStages.get(newStageIndex));
                userRepository.save(currentUser);
                var text = "Вы вернулись на шаг назад! Можно дать новый ответ на предпоследний вопрос: ";
                return makeSendMessage(chatId, text);
            }
            default -> {
                return unknownCommand(chatId);
            }
        }
    }

    public SendMessage makeUserRequest(String message, User currentUser) {
        Request request = getRequest(currentUser).orElse(new Request(currentUser));
        String text = switch (currentUser.getStage()) {
            case SPECIALIST_AGE, SPECIALIST_GENDER -> { //SPECIALIST_AGE достижим только при нажатии "/Back"
                request.setSpecAge(message);
                currentUser.setStage(RequestStages.METHOD);
                yield "Пожалуйста, введите требования к полу специалиста: ";
            }
            case METHOD -> {
                request.setGender(message);
                currentUser.setStage(RequestStages.BUDGET);
                yield "Пожалуйста, введите требования к методу, в котором работает специалист";
            }
            case BUDGET -> {
                request.setMethodTherapy(message);
                currentUser.setStage(RequestStages.CLIENT_AGE);
                yield "Пожалуйста, введите ваши пожелания касаемо стоимости сессий и их частоты";
            }
            case CLIENT_AGE -> {
                request.setBudget(message);
                currentUser.setStage(RequestStages.CLIENT_GENDER);
                yield "Пожалуйста, введите ваш возраст: ";
            }
            case CLIENT_GENDER -> {
                request.setClientAge(message);
                currentUser.setStage(RequestStages.CLIENT_DIAGNOSIS);
                yield "Пожалуйста, введите ваш пол: ";
            }
            case CLIENT_DIAGNOSIS -> {
                request.setClientGender(message);
                currentUser.setStage(RequestStages.CLIENT_REQUEST);
                yield "Пожалуйста, укажите психиатрические диагнозы, " +
                        "которые были поставлены психиатром или которые вы у себя подозреваете. " +
                        "А так же прочие, если они связаны с вашим запросом: ";
            }
            case CLIENT_REQUEST -> {
                request.setDiagnosis(message);
                currentUser.setStage(RequestStages.EXTRA);
                yield "Пожалуйста, опишите ваш запрос (например: " +
                        "\"Психиатр поставил мне эти диагнозы и рекомендовал обратиться к КПТ-психологу\" \n" +
                        "\"Я хочу уменьшить выраженность депрессии\" \n" +
                        "\"Я переживаю развод и мне нужна помощь\"):";
            }
            case EXTRA -> {
                request.setClientRequest(message);
                currentUser.setStage(RequestStages.SEND_REQUEST);
                yield "Здесь вы можете указать дополнительные пожелания к специалисту, " +
                        "другие сведения о себе и любую информацию, " +
                        "которую посчитаете необходимой: ";
            }
            case SEND_REQUEST -> {
                request.setExtra(message);
                currentUser.setStage(RequestStages.SEND_APPROVED);
                yield "Отправить запрос? Чтобы отправить - введите команду \"/send\" \n" +
                        "Получившийся запрос: \n\n" + request;
            }
            case SEND_APPROVED -> {
                if (message.equals("/send")) {
                    currentUser.setIsCreationRequest(false);
                    yield "Запрос отправлен!";
                } else {
                    yield "Команда не распознана. Чтобы отправить запрос, введите \"/send\". " +
                            "Чтобы удалить запрос, введите \"/delete\"";
                }
            }
        };
        requestRepository.save(request);
        userRepository.save(currentUser);
        return makeSendMessage(currentUser.getId(), text);
    }

    private Optional<Request> getRequest(User currentUser) {
        final var requests = currentUser.getRequests();
        if (requests.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(requests.get(requests.size() - 1));
    }

    private SendMessage helpCommand(Long chatId) {
        var text = """
                Вот как я работаю:
                Сперва введите команду (можно воспользоваться меню)
                /request - начать составление запроса
                
                После введения этой команды, я задам вам несколько вопросов, и затем, после вашего подтверждения, 
                отправлю запрос в чат "Все свои". Участникам чата не будет доступна информация о том, кто именно создал
                запрос. Все отклики будут приходить мне, а я - пересылать вам.
                Я сохраню ваш запрос в своей памяти. Однако я не храню ваш никнейм или другие данные для опознавания.
                Кроме того, вы можете удалить запрос из моей памяти с помощью команды
                /delete - удалить запрос (Внимание! Запрос не будет удален из чата "Все свои", если он уже отправлен)                
                
                Дополнительные команды:
                /help - показать это сообщение               
                """
                ;
        return makeSendMessage(chatId, text);
    }

    private SendMessage unknownCommand(Long chatId) {
        var text = "Не удалось распознать команду!";
        return makeSendMessage(chatId, text);
    }


    private SendMessage makeSendMessage(Long chatId, String text) {
        return new SendMessage(String.valueOf(chatId), text);
    }
}