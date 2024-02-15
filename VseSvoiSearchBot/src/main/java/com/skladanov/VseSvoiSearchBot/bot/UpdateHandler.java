package com.skladanov.VseSvoiSearchBot.bot;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@NoArgsConstructor
class UpdateHandler {
    private static final String START = "/start";
    private static final String REQUEST = "/request";
    private static final Long TEST_CHAT_ID = -1002107664061L;
    private boolean isCreationRequest = false;
    private RequestStages stage = RequestStages.SPECIALIST_AGE;

    public SendMessage handleUpdate(Update update) {
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        if (isCreationRequest) {
            String text = switch (stage) {
                case SPECIALIST_AGE -> {
                    stage = RequestStages.SPECIALIST_GENDER;
                    yield "Пожалуйста, введите требования к возрасту специалиста: ";
                }
                case SPECIALIST_GENDER -> {
                    stage = RequestStages.METHOD;
                    yield "Пожалуйста, введите требования к полу специалиста: ";
                }
                case METHOD -> {
                    stage = RequestStages.BUDGET;
                    yield "Пожалуйста, введите требования к методу, в котором работает специалист";
                }
                case BUDGET -> {
                    stage = RequestStages.CLIENT_AGE;
                    yield "Пожалуйста, введите ваши пожелания касаемо стоимости сессий и их частоты";
                }
                case CLIENT_AGE -> {
                    stage = RequestStages.CLIENT_GENDER;
                    yield "Пожалуйста, введите ваш возраст: ";
                }
                case CLIENT_GENDER -> {
                    stage = RequestStages.CLIENT_DIAGNOSIS;
                    yield "Пожалуйста, введите ваш пол: ";
                }
                case CLIENT_DIAGNOSIS -> {
                    stage = RequestStages.CLIENT_REQUEST;
                    yield "Пожалуйста, укажите психиатрические диагнозы, " +
                            "которые были поставлены психиатром или которые вы у себя подозреваете. " +
                            "А так же прочие, если они связаны с ваши запросом: ";
                }
                case CLIENT_REQUEST -> {
                    stage = RequestStages.EXTRA;
                    yield "Пожалуйста, опишите ваш запрос (например: \"Психиатр поставил мне эти диагнозы и рекомендовал обратиться к КПТ-психологу\" \n" +
                            "\"Я хочу уменьшить выраженность депрессии\" \n" +
                            "\"Я переживаю развод и мне нужна помощь\"):";
                }
                case EXTRA -> {
                    stage = RequestStages.SEND_REQUEST;
                    yield "Здесь вы можете указать дополнительные пожелания к специалисту, " +
                            "другие сведения о себе и любую информацию, " +
                            "которую посчитаете необходимой: ";
                }
                case SEND_REQUEST -> {
                    stage = RequestStages.SEND_APPROVED;
                    yield "Отправить запрос?";
                }
                case SEND_APPROVED -> {
                    isCreationRequest = false;
                    yield "Запрос отправлен!";
                }
            };
            return makeSendMessage(chatId, text);
        }
        switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                return startCommand(chatId, userName);
            }
            case REQUEST -> {
                isCreationRequest = true;
                var text = "Отлично! Сейчас я попрошу вас заполнить несколько полей, " +
                        "чтобы составить запрос, после чего вы сможете подтвердить отправку. " +
                        "Если вы не хотите или не знаете, что написать в поле, " +
                        "просто поставьте прочерк(-) или любой другой символ и переходите к следующему вопросу. " +
                        "Итак, для начала введите требования к возрасту специалиста: ";
                stage = RequestStages.SPECIALIST_GENDER;
                return makeSendMessage(chatId, text);
            }
            default -> {
                return unknownCommand(chatId);
            }
        }
    }

    private SendMessage startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в бот, %s!
                                
                Здесь Вы сможете найти специалиста из сообщества "Все свои" под свой запрос.
                А еще вы очень красивая, вы знали?
                Для этого воспользуйтесь командами:
                /request - вместе с командой напишите ваш запрос
                                
                Дополнительные команды:
                /help - получение справки
                """;
        return makeSendMessage(chatId, String.format(text, userName));
    }

    /*private void requestCommand(Long chatId, String message) {
        sendMessage(TEST_CHAT_ID, message + "вот так запрос");
        sendMessage(chatId, "Ваш запрос отправлен: " + message + "вот так запрос");
    }*/

    private SendMessage unknownCommand(Long chatId) {
        var text = "Не удалось распознать команду!";
        return makeSendMessage(chatId, text);
    }

    private SendMessage makeSendMessage(Long chatId, String text) {
        return new SendMessage(String.valueOf(chatId), text);
    }
}
