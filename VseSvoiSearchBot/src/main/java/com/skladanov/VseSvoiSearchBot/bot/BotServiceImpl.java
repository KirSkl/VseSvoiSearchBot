package com.skladanov.VseSvoiSearchBot.bot;

import com.skladanov.VseSvoiSearchBot.bot.model.Request;
import com.skladanov.VseSvoiSearchBot.bot.model.Response;
import com.skladanov.VseSvoiSearchBot.bot.model.User;
import com.skladanov.VseSvoiSearchBot.bot.repo.RequestRepository;
import com.skladanov.VseSvoiSearchBot.bot.repo.ResponseRepository;
import com.skladanov.VseSvoiSearchBot.bot.repo.UserRepository;
import com.skladanov.VseSvoiSearchBot.bot.util.SendMessageWithData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import static com.skladanov.VseSvoiSearchBot.bot.Commands.*;

import java.util.Optional;

/*
 * todo: реализовать получение всех своих запросов/ откликов
 * реализовать удаление отдельных запросов и откликов
 * реализовать удаление всех запросов/откликов
 * реализовать команду старт как сброс текущих операций
 * добавить команды в меню
 * реализовать редактирование сообщений и отмену запроса/отклика
 *
 * */

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {
    private static final Long DECRYPT_KEY = 31L;
    private static final int BACK_INDEX = 2; //чтобы вернуться на шаг назад, нужно изменить состояние запроса на 2 значения назад
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final ResponseRepository responseRepository;

    @Override
    @Transactional
    public SendMessageWithData makeAnswer(Update update) {
        final User currentUser = getUserOrSave(update);
        final String message = update.getMessage().getText().stripLeading().toLowerCase();
        if (message.charAt(0) == '/') {
            return checkCommand(message, currentUser);
        }
        if (currentUser.getIsAnswering()) {
            return makeResponse(message, currentUser);
        }
        if (currentUser.getIsCreationRequest()) {
            return makeUserRequest(message, currentUser);
        }
        return unknownCommand(currentUser.getId().toString());
    }

    private User getUserOrSave(Update update) {
        Long chatId = update.getMessage().getChatId();
        Optional<User> userOptional = userRepository.findById(chatId);
        User user;
        if (userOptional.isEmpty()) {
            user = new User(chatId);
            userRepository.save(user);
        } else {
            user = userOptional.get();
        }
        return user;
    }

    private SendMessageWithData checkCommand(String message, User currentUser) {
        final var chatId = currentUser.getId().toString();

        return switch (message) {
            case HELP -> handleHelp(chatId);
            case DELETE_REQUEST -> handleDeleteRequest(chatId, currentUser);
            case DELETE_RESPONSE-> handleDeleteResponse(chatId, currentUser);
            case REQUEST -> handleRequest(chatId, currentUser);
            case BACK -> handleBack(chatId, currentUser);
            case RESPONSE -> handleResponse(chatId, currentUser);
            default -> unknownCommand(chatId);
        };
    }

    private SendMessageWithData handleHelp(String chatId) {
        return helpCommand(chatId);
    }

    private SendMessageWithData handleDeleteRequest(String chatId, User currentUser) {
        final var request = getRequest(currentUser);
        var text = "";
        if (request.isPresent()) {
            requestRepository.delete(request.get());
            text = "Запрос удален. Чтобы составить новый запрос, введите команду \"/request\"";
        } else {
            text = "На данный момент у вас нет сохраненного запроса. " +
                    "Чтобы составить новый запрос, введите команду \"/request\"";
        }
        return new SendMessageWithData(chatId, text);
    }

    private SendMessageWithData handleDeleteResponse(String chatId, User currentUser) {
        final var response = getResponse(currentUser);
        var text = "";
        if (response.isPresent()) {
            responseRepository.delete(response.get());
            text = "Отклик удален. Чтобы отправить новый отклик, введите команду \"/response\"";
        } else {
            text = "На данный момент у вас нет сохраненного запроса. " +
                    "Чтобы составить новый запрос, введите команду \"/response\"";
        }
        return new SendMessageWithData(chatId, text);
    }

    private SendMessageWithData handleRequest(String chatId, User currentUser) {
        var text = "";
        if (currentUser.getIsCreationRequest()) {
            text = "Вы уже в процессе заполнения запроса на поиск. " +
                    "Если хотите удалить текущий запрос, введите команду \"/delete_req\"";
        } else {
            currentUser.setIsCreationRequest(true);
            userRepository.save(currentUser);
            text = "Отлично! Сейчас я попрошу вас заполнить несколько полей, чтобы составить запрос, " +
                    "после чего вы сможете подтвердить отправку...";
        }
        return new SendMessageWithData(chatId, text);
    }

    private SendMessageWithData handleBack(String chatId, User currentUser) {
        if (currentUser.getIsCreationRequest()) {
            return handleBackInRequest(chatId, currentUser);
        }
        if (currentUser.getIsAnswering()) {
            return handleBackInResponse(chatId, currentUser);
        }
        return new SendMessageWithData(chatId, "На данный момент возвращаться некуда");
    }

    private SendMessageWithData handleBackInResponse(String chatId, User currentUser) {
        ResponseStages responseStages = currentUser.getResponseStages();
        if (responseStages.equals(ResponseStages.NUMBER) ||
                responseStages.equals(ResponseStages.CONTENT)) {
            return new SendMessageWithData(chatId,
                    "На данный момент у вас нет заполненных полей отклика");
        }
        final var stages = ResponseStages.values();
        final var newStageIndex = responseStages.ordinal() - BACK_INDEX; //
        currentUser.setResponseStages(stages[newStageIndex]);
        userRepository.save(currentUser);
        return new SendMessageWithData(chatId,
                "Вы вернулись на шаг назад! Можно дать новый ответ на предпоследний вопрос: ");
    }


    private SendMessageWithData handleBackInRequest(String chatId, User currentUser) {
        RequestStages requestStage = currentUser.getRequestStage();
        if (requestStage.equals(RequestStages.SPECIALIST_GENDER) ||
                requestStage.equals(RequestStages.SPECIALIST_AGE)) {
            var text = "На данный момент у вас нет заполненных полей запроса";
            return new SendMessageWithData(chatId, text);
        }
        final var stages = RequestStages.values();
        final var newStageIndex = requestStage.ordinal() - BACK_INDEX; //
        currentUser.setRequestStage(stages[newStageIndex]);
        userRepository.save(currentUser);
        return new SendMessageWithData(chatId, "Вы вернулись на шаг назад! " +
                "Можно дать новый ответ на предпоследний вопрос: ");
    }

    private SendMessageWithData handleResponse(String chatId, User currentUser) {
        if (currentUser.getIsAnswering()) {
            return new SendMessageWithData(chatId, "Вы уже в процессе отклика на заявку. " +
                    "Если хотите удалить текущий отклик, введите команду \"/delete\"");
        }
        currentUser.setIsAnswering(true);
        userRepository.save(currentUser);
        return new SendMessageWithData(chatId, "Отлично! Введите номер запроса (указан в сообщении в чате):");
    }

    public SendMessageWithData makeUserRequest(String message, User currentUser) {
        Request request = getRequest(currentUser).orElse(new Request(currentUser));
        final var sendMessageWithData = new SendMessageWithData();
        String text = switch (currentUser.getRequestStage()) {
            case SPECIALIST_AGE, SPECIALIST_GENDER -> { //SPECIALIST_AGE достижим только при нажатии "/Back"
                request.setSpecAge(message);
                currentUser.setRequestStage(RequestStages.METHOD);
                yield "Пожалуйста, введите требования к полу специалиста: ";
            }
            case METHOD -> {
                request.setGender(message);
                currentUser.setRequestStage(RequestStages.FORM);
                yield "Пожалуйста, введите требования к методу, в котором работает специалист";
            }
            case FORM -> {
                request.setMethodTherapy(message);
                currentUser.setRequestStage(RequestStages.BUDGET);
                yield "Пожалуйста, введите пожеланию к формату работы (очно/онлайн). " +
                        "Если требуется очный формат, так же введите название населенного пункта. Например: \n" +
                        "\"Онлайн или очно в Порту(Португалия)\"";
            }
            case BUDGET -> {
                request.setFormatTherapy(message);
                currentUser.setRequestStage(RequestStages.CLIENT_AGE);
                yield "Пожалуйста, введите ваши пожелания касаемо стоимости сессий и их частоты";
            }
            case CLIENT_AGE -> {
                request.setBudget(message);
                currentUser.setRequestStage(RequestStages.CLIENT_GENDER);
                yield "Пожалуйста, введите ваш возраст: ";
            }
            case CLIENT_GENDER -> {
                request.setClientAge(message);
                currentUser.setRequestStage(RequestStages.CLIENT_DIAGNOSIS);
                yield "Пожалуйста, введите ваш пол: ";
            }
            case CLIENT_DIAGNOSIS -> {
                request.setClientGender(message);
                currentUser.setRequestStage(RequestStages.CLIENT_REQUEST);
                yield "Пожалуйста, укажите психиатрические диагнозы, " +
                        "которые были поставлены психиатром или которые вы у себя подозреваете. " +
                        "А так же прочие, если они связаны с вашим запросом: ";
            }
            case CLIENT_REQUEST -> {
                request.setDiagnosis(message);
                currentUser.setRequestStage(RequestStages.EXTRA);
                yield "Пожалуйста, опишите ваш запрос (например: " +
                        "\"Психиатр поставил мне эти диагнозы и рекомендовал обратиться к КПТ-психологу\" \n" +
                        "\"Я хочу уменьшить выраженность депрессии\" \n" +
                        "\"Я переживаю развод и мне нужна помощь\"):";
            }
            case EXTRA -> {
                request.setClientRequest(message);
                currentUser.setRequestStage(RequestStages.SEND_REQUEST);
                yield "Здесь вы можете указать дополнительные пожелания к специалисту, " +
                        "другие сведения о себе и любую информацию, " +
                        "которую посчитаете необходимой: ";
            }
            case SEND_REQUEST -> {
                request.setExtra(message);
                currentUser.setRequestStage(RequestStages.SEND_APPROVED);
                yield "Отправить запрос? Чтобы отправить - введите команду \"/send\" \n" +
                        "Получившийся запрос: \n\n" + request;
            }
            case SEND_APPROVED -> {
                if (message.equals("/send")) {
                    currentUser.setIsCreationRequest(false);
                    final var requests = currentUser.getRequests();
                            sendMessageWithData.setRequest(requests.get(requests.size()-1));
                    yield "Готово! Отправил ваш запрос в чат!";
                } else {
                    yield "Команда не распознана. Чтобы отправить запрос, введите \"/send\". " +
                            "Чтобы удалить запрос, введите \"/delete\"";
                }
            }
        };
        requestRepository.save(request);
        userRepository.save(currentUser);
        sendMessageWithData.setChatId(currentUser.getId());
        sendMessageWithData.setText(text);
        return sendMessageWithData;
    }

    private SendMessageWithData makeResponse(String message, User currentUser) {
        final var sendMessageWithData = new SendMessageWithData();
        var response = getResponse(currentUser).orElse(new Response(currentUser));
        String text = switch (currentUser.getResponseStages()) {
            case NUMBER, CONTACTS -> {
                final var number = Long.parseLong(message) / DECRYPT_KEY;
                final var request = requestRepository.findById(number);
                if (request.isPresent()) {
                    response.setRequest(request.get());
                    currentUser.setResponseStages(ResponseStages.CONTENT);
                    yield "Запрос найден! Пожалуйста, введите в свободной форме свои контактные данные, " +
                            "которые будут пересланы пользователю " +
                            "(например, номер телефона, никнейм в мессенджерах, адрес сайта):";
                } else {
                    yield "Запрос с данным номером не найден. Попробуйте ввести повторно: ";
                }
            }
            case CONTENT -> {
                response.setContacts(message);
                currentUser.setResponseStages(ResponseStages.SEND_RESPONSE);
                yield "Ага, записал. Введите, пожалуйста, текст отклика, который я перешлю " +
                        "клиенту вместе с контактами. Например: " +
                        "\"Добрый день! Меня зовут Анна Иванова, я клинический психолог. " +
                        "Работаю в КПТ, АСТ и схема-терапии. Готова поработать с вашей проблемой, " +
                        "принимаю очно и онлайн. Стоимость - 5000 за сессию 50мин\"";
            }
            case SEND_RESPONSE -> {
                response.setContent(message);
                currentUser.setResponseStages(ResponseStages.SEND_APPROVED);
                yield "Готово! Смотрите, что получилось:\n" +
                        response + "\n Отправляем? Если да, введите /sendResponse";
            }
            case SEND_APPROVED -> {
                if (message.equals("/sendResponse")) {
                    currentUser.setIsAnswering(false);
                    final var responses = currentUser.getResponses();
                    sendMessageWithData.setResponse(responses.get(responses.size()-1));
                    yield "Отклик отправлен!";
                } else {
                    yield "Команда не распознана. Чтобы отправить отклик, введите \"/sendResponse\". " +
                            "Чтобы удалить отклик, введите \"/delete\"";
                }
            }
        };
        responseRepository.save(response);
        userRepository.save(currentUser);
        sendMessageWithData.setChatId(currentUser.getId());
        sendMessageWithData.setText(text);
        return sendMessageWithData;
    }

    private Optional<Request> getRequest(User currentUser) {
        final var requests = currentUser.getRequests();
        if (requests.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(requests.get(requests.size() - 1));
    }

    private Optional<Response> getResponse(User currentUser) {
        final var responses = currentUser.getResponses();
        if (responses.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(responses.get(responses.size() - 1));
    }

    private SendMessageWithData helpCommand(String chatId) {
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
                /back - изменить ответ на предыдущий вопрос при формировании запроса               
                """;
        return new SendMessageWithData(chatId, text);
    }

    private SendMessageWithData unknownCommand(String chatId) {
        return new SendMessageWithData(chatId, "Не удалось распознать команду!");
    }

    /*private SendMessage makeSendMessage(String chatId, String text) {
        return new SendMessage(chatId, text);
    }*/
}